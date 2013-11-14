package com.jivesoftware.os.jive.utils.ordered.id;

import com.jivesoftware.os.jive.utils.ordered.id.OrderIdProviderImpl;
import com.jivesoftware.os.jive.utils.ordered.id.OrderIdProvider;
import com.jivesoftware.os.jive.utils.ordered.id.SessionIdPacker;
import com.jivesoftware.os.jive.utils.ordered.id.SnowflakeIdPacker;
import com.jivesoftware.os.jive.utils.ordered.id.TimestampProvider;
import com.jivesoftware.os.jive.utils.ordered.id.IdGenerationException;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 *
 */
public class OrderIdProviderImplTest {

    @Test
    public void testMonotonicity() throws Exception {
        OrderIdProvider orderIdProvider = new OrderIdProviderImpl(1);
        long firstOrderId = orderIdProvider.nextId();
        long secondOrderId = orderIdProvider.nextId();
        int compare = (firstOrderId < secondOrderId) ? -1 : ((firstOrderId == secondOrderId) ? 0 : 1);
        Assert.assertTrue(compare < 0);
    }

    @Test
    public void testClockMovingBackwards() throws Exception {
        final AtomicBoolean returnZeroTime = new AtomicBoolean();

        OrderIdProvider orderIdProvider = new OrderIdProviderImpl(1, new SnowflakeIdPacker(), new TimestampProvider() {
            @Override
            public long getTimestamp() {
                if (!returnZeroTime.get()) {
                    return System.currentTimeMillis();
                } else {
                    return 0;
                }
            }
        });

        orderIdProvider.nextId();
        returnZeroTime.set(true);

        try {
            orderIdProvider.nextId();
            Assert.fail("Expected IdGenerationException when clock moved backwards");
        } catch (IdGenerationException e) {
        }
    }

    static int compare(long x, long y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    @Test
    public void testSortOrder() throws Exception {

        int z = 32634;
        SnowflakeIdPacker idPacker = new SnowflakeIdPacker();
        Assert.assertTrue(idPacker.pack(z, z, z) < idPacker.pack(z, z, z + 1));
        Assert.assertTrue(idPacker.pack(z, z, z) < idPacker.pack(z, z + 1, z));
        Assert.assertTrue(idPacker.pack(z, z, z) < idPacker.pack(z + 1, z, z));
        Assert.assertTrue(idPacker.pack(z, z, z) < idPacker.pack(z + 1, z, z + 1));
        Assert.assertTrue(idPacker.pack(z, z, z) < idPacker.pack(z + 1, z + 1, z + 1));

    }

    @Test
    public void testSnowflakePackAndUnpack() throws Exception {
        SnowflakeIdPacker idPacker = new SnowflakeIdPacker();
        long packed = idPacker.pack(1, 2, 3);
        long[] unpacked = idPacker.unpack(packed);
        Assert.assertEquals(unpacked[0], 1);
        Assert.assertEquals(unpacked[1], 2);
        Assert.assertEquals(unpacked[2], 3);

    }

    @Test
    public void testSessionPackAndUnpack() throws Exception {
        SessionIdPacker idPacker = new SessionIdPacker();
        long packed = idPacker.pack(1, 2, 3);
        long[] unpacked = idPacker.unpack(packed);
        Assert.assertEquals(unpacked[0], 1);
        Assert.assertEquals(unpacked[1], 2);
        Assert.assertEquals(unpacked[2], 3);

        packed = idPacker.pack(1, (int) Math.pow(2, 14) - 1, 3);
        unpacked = idPacker.unpack(packed);
        Assert.assertEquals(unpacked[0], 1);
        Assert.assertEquals(unpacked[1], (int) Math.pow(2, 14) - 1);
        Assert.assertEquals(unpacked[2], 3);

    }

    @SuppressWarnings ("unused")
    @Test (expectedExceptions = IllegalArgumentException.class)
    public void testWriterIdUnderBound() {
        new OrderIdProviderImpl(-1);
    }

    @SuppressWarnings ("unused")
    @Test (expectedExceptions = IllegalArgumentException.class)
    public void testWriterIdOverBounds() {
        new OrderIdProviderImpl(1024);
    }

    @Test (groups = "slow")
    public void testMultiThreadedUniqueIds() throws InterruptedException {
        final Set<Long> generated = new ConcurrentSkipListSet<>();
        final OrderIdProviderImpl provider = new OrderIdProviderImpl(2);
        final int numIterations = 1000000;

        int numThreads = 10;
        ExecutorService runner = Executors.newFixedThreadPool(numThreads);
        final AtomicLong failedId = new AtomicLong(-1);

        Runnable task = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < numIterations; i++) {

                    long next = provider.nextId();

                    if (!generated.add(next)) {
                        failedId.set(next);
                        break;
                    }
                }
            }
        };

        for (int i = 0; i < numThreads; i++) {
            runner.submit(task);
        }

        runner.shutdown();
        runner.awaitTermination(1, TimeUnit.DAYS);


        Long failedVal = failedId.get();
        if (failedVal != -1) {
            long[] fields = provider.unpack(failedVal);

            StringBuilder failedMessage = new StringBuilder("Duplicate id returned with timestamp ").
                append(fields[0]).append(", writer id ").append(fields[1]).append(", and sequence number ").
                append(fields[2]);

            Assert.fail(failedMessage.toString());
        }
    }
}
