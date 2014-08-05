package com.jivesoftware.os.jive.utils.permit;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.CurrentTimestamper;
import com.jivesoftware.os.jive.utils.row.column.value.store.inmemory.RowColumnValueStoreImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class PermitProviderImplConcurrencyTest {

    private static final Logger logger = LoggerFactory.getLogger(PermitProviderImplConcurrencyTest.class);

    private static final int NUM_THREADS = 10;
    private static final long EXPIRES = 10;
    private static final long DURATION = 5 * 1000;

    private final RowColumnValueStore<String, String, Integer, Permit, RuntimeException> store = new RowColumnValueStoreImpl<>();
    private final Random random = new Random(System.currentTimeMillis());

    private final AtomicReference<CountDownLatch> signal = new AtomicReference<>(new CountDownLatch(NUM_THREADS));
    private final Object lock = new Object();

    @Test(groups = "slow")
    public void testConcurrentUsageOfPermitProviderNeverFails() throws Exception {
        // Repeatedly request permits from a number of threads. The guarantee made by PermitProviderImpl is that no two
        // clients can ever share the same unexpired permit.

        List<PermitClient> clients = new ArrayList<>(NUM_THREADS);
        List<Thread> threads = new ArrayList<>(NUM_THREADS);
        for (int i = 0; i < NUM_THREADS; i++) {
            PermitClient client = new PermitClient(i);
            clients.add(client);
            threads.add(new Thread(client));
        }

        for (Thread thread : threads) {
            thread.start();
        }

        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < DURATION) {
            Map<Integer, PermitClient> permitMap = Maps.newTreeMap();

            signal.get().await();

            long now = System.currentTimeMillis();
            for (PermitClient client : clients) {
                Permit permit = client.permit.get();
                if (permit != null) {
                    if (permitMap.containsKey(permit.id)) {
                        PermitClient competitor = permitMap.get(permit.id);
                        logger.debug("Clients " + client.id + " and " + competitor.id + " have the same permit " + permit.id + ".");

                        assertOneOrBothPermitsExpired(client, competitor, now);

                        logger.debug("One or both of the permits were expired. Continuing...");
                    }
                    permitMap.put(permit.id, client);
                }
            }

            synchronized (lock) {
                signal.set(new CountDownLatch(NUM_THREADS));
                lock.notifyAll();
            }
        }

        for (Thread thread : threads) {
            thread.interrupt();
            thread.join();
        }
    }

    private void assertOneOrBothPermitsExpired(PermitClient client1,
            PermitClient client2, long now) {
        long expires1 = client1.permit.get().issued + EXPIRES;
        long expires2 = client2.permit.get().issued + EXPIRES;
        assertTrue(
                expires1 <= now || expires2 <= now,
                "Neither permit is expired! "
                + "Now: " + now + "; "
                + "Client " + client1.id + " expires at " + expires1 + "; "
                + "Client " + client2.id + " expires at " + expires2
        );
    }

    private class PermitClient implements Runnable {

        private final PermitProvider permitProvider;

        final int id;
        final AtomicReference<Permit> permit = new AtomicReference<>();
        final ConstantPermitConfig permitConfig = new ConstantPermitConfig(0, NUM_THREADS, EXPIRES);

        public PermitClient(int id) {
            this.id = id;

            try {
                permitProvider = new PermitProviderImpl("bob", store, new CurrentTimestamper());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void run() {
            long nextRenewChance = 0;
            while (true) {
                long now = System.currentTimeMillis();

                if (permit.get() == null || isPermitExpired(now)) {
                    List<Permit> got = permitProvider.requestPermit("t", "permitGroup", permitConfig, 1);
                    if (!got.isEmpty()) {
                        permit.set(got.get(0));
                    } else {
                        throw new RuntimeException("OutOfPermits");
                    }
                    nextRenewChance = now + EXPIRES / 2;

                    log("issued permit " + permit.get().id);
                }

                if (shouldTryRenew(now, nextRenewChance)) {
                    if (nextRenewChance == now + EXPIRES - 1) {
                        log("possibly renewing immediately before expiration");
                    }

                    if (maybeRenew()) {
                        nextRenewChance = now + EXPIRES / 2;
                        log("renewed permit " + permit.get().id);
                    } else {
                        // Try to renew right before the permit expires. It's unlikely this will happen, but in the
                        // case that it does, it's possible that we could try to renew while another client tries to
                        // claim the expired permit.
                        nextRenewChance = now + EXPIRES - 1;
                        log("chose not to renew");
                    }
                }

                synchronized (lock) {
                    try {
                        signal.get().countDown();
                        lock.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }

        public boolean isPermitExpired(long now) {
            return permit.get() == null || now - permit.get().issued > EXPIRES;
        }

        private boolean shouldTryRenew(long now, long nextRenewChance) {
            return permit.get() != null && now >= nextRenewChance;
        }

        private boolean maybeRenew() {
            if (random.nextFloat() <= 0.25f) {
                List<Optional<Permit>> renewed = permitProvider.renewPermit(Arrays.asList(permit.get()));
                Optional<Permit> got = renewed.get(0);
                if (got.isPresent()) {
                    permit.set(got.get());
                    return true;
                }
            }
            return false;
        }

        private void log(String message) {
            logger.debug("Client " + id + ": " + message);
        }
    }
}
