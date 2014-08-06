/*
 * Copyright 2014 Jive Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jivesoftware.os.jive.utils.permit;

import com.google.common.base.Optional;
import com.jivesoftware.os.jive.utils.permit.PermitProviderImplInitializer.PermitProviderConfig;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.SetOfSortedMapsImplInitializer;
import com.jivesoftware.os.jive.utils.row.column.value.store.inmemory.InMemorySetOfSortedMapsImplInitializer;
import com.jivesoftware.os.jive.utils.row.column.value.store.tests.EmbeddedHBase;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.merlin.config.BindInterfaceToConfiguration;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author jonathan
 */
public class PermitProviderAgainstHBaseTest {

    EmbeddedHBase embeddedHBase;
    PermitProvider permitProvider;

    @BeforeMethod
    public void setup() throws Exception {

        if (permitProvider == null) {
            embeddedHBase = new EmbeddedHBase();
            //embeddedHBase.start(false);

            SetOfSortedMapsImplInitializer sos = new InMemorySetOfSortedMapsImplInitializer();
            //SetOfSortedMapsImplInitializer sos = new HBaseSetOfSortedMapsImplInitializer(embeddedHBase.getConfiguration());

            PermitProviderConfig config = BindInterfaceToConfiguration.bindDefault(PermitProviderConfig.class);
            permitProvider = new PermitProviderImplInitializer().initPermitProvider(config, sos);
        }
    }

    @AfterMethod
    public void teardown() throws Exception {
        //embeddedHBase.stop();
    }

    @Test
    public void basicTest() throws IOException, InterruptedException {

        ConstantPermitConfig permitConfig = new ConstantPermitConfig(0, 1, 100);
        List<Permit> issuedPermits = permitProvider.getAllIssuedPermits("basicTest", "group", permitConfig);
        Assert.assertEquals(0, issuedPermits.size());

        List<Permit> got = permitProvider.requestPermit("basicTest", "group", permitConfig, 1);
        Assert.assertEquals(got.size(), 1);

        List<Permit> empty = permitProvider.requestPermit("basicTest", "group", permitConfig, 1);
        Assert.assertEquals(empty.size(), 0);

        List<Optional<Permit>> renewed = permitProvider.renewPermit(Arrays.asList(got.get(0)));
        Assert.assertEquals(got.size(), 1);
        Assert.assertTrue(renewed.get(0).isPresent());

        issuedPermits = permitProvider.getAllIssuedPermits("basicTest", "group", permitConfig);
        Assert.assertEquals(1, issuedPermits.size());

        permitProvider.releasePermit(Arrays.asList(renewed.get(0).get()));

        issuedPermits = permitProvider.getAllIssuedPermits("basicTest", "group", permitConfig);
        Assert.assertEquals(0, issuedPermits.size());

        got = permitProvider.requestPermit("basicTest", "group", permitConfig, 1);
        Assert.assertEquals(got.size(), 1);

        Thread.sleep(500); // lets permit expire

        renewed = permitProvider.renewPermit(Arrays.asList(got.get(0)));
        Assert.assertEquals(got.size(), 1);
        Assert.assertFalse(renewed.get(0).isPresent());

        got = permitProvider.requestPermit("basicTest", "group", permitConfig, 1);
        Assert.assertEquals(got.size(), 1);
    }

    @Test
    public void hogPermitsTest() throws IOException, InterruptedException {

        ConstantPermitConfig permitConfig = new ConstantPermitConfig(0, 10, 100);
        List<Permit> issuedPermits = permitProvider.getAllIssuedPermits("hogPermitsTest", "group", permitConfig);
        Assert.assertEquals(0, issuedPermits.size());

        List<Permit> ten = permitProvider.requestPermit("hogPermitsTest", "group", permitConfig, 100);
        System.out.println(ten);
        Assert.assertEquals(ten.size(), 10);

        List<Permit> zero = permitProvider.requestPermit("hogPermitsTest", "group", permitConfig, 100);
        Assert.assertEquals(zero.size(), 0);

        Thread.sleep(500); // lets permit expire

        ten = permitProvider.requestPermit("hogPermitsTest", "group", permitConfig, 100);
        Assert.assertEquals(ten.size(), 10);

    }

    @Test
    public void concurrentPermitsTest() throws IOException, InterruptedException {
        int maxPermits = 10;
        int expires = 100;
        ConstantPermitConfig permitConfig = new ConstantPermitConfig(0, maxPermits, expires);

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(maxPermits);
        List<PermitGrabber> permitGrabbers = new ArrayList<>();
        for (int i = 0; i < maxPermits; i++) {
            PermitGrabber permitGrabber = new PermitGrabber("concurrentPermitsTest", "booya", permitProvider, permitConfig);
            permitGrabbers.add(permitGrabber);
            executorService.scheduleAtFixedRate(permitGrabber, 0, (expires / 4) * 3, TimeUnit.MILLISECONDS);
        }

        long start = System.currentTimeMillis();
        int count = 0;
        while (System.currentTimeMillis() - start < 5000) {
            Set<Integer> permitIds = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
            for (PermitGrabber permitGrabber : permitGrabbers) {
                Optional<Integer> optionalId = permitGrabber.permitId();
                if (optionalId.isPresent()) {
                    Integer id = optionalId.get();
                    Assert.assertFalse(permitIds.contains(id));
                    permitIds.add(id);
                }
            }
            count += permitIds.size();
            System.out.println("active permits:" + permitIds.size() + " out of " + maxPermits + " permits:" + permitIds);
            Thread.sleep(2);
        }
        System.out.println("count:" + count);
        Assert.assertTrue(count > 0);
        executorService.shutdown();
    }

    static class PermitGrabber implements Runnable {

        AtomicReference<Permit> permit = new AtomicReference<>();
        private final String tenant;
        private final String permitGroup;
        private final PermitProvider permitProvider;
        private final ConstantPermitConfig permitConfig;

        public PermitGrabber(String tenant, String permitGroup, PermitProvider permitProvider, ConstantPermitConfig permitConfig) {
            this.tenant = tenant;
            this.permitGroup = permitGroup;
            this.permitProvider = permitProvider;
            this.permitConfig = permitConfig;
        }

        @Override
        public void run() {
            try {
                Permit p = permitProvider.isExpired(permit.get()).orNull();
                if (p == null) {
                    permit.set(null);
                    p = null;
                }

                if (p == null) {
                    List<Permit> requestPermit = permitProvider.requestPermit(tenant, permitGroup, permitConfig, 1);
                    if (requestPermit.size() > 0) {
                        p = requestPermit.get(0);
                        //System.out.println("Requested:" + p);
                        permit.set(p);
                    }

                } else {
                    List<Optional<Permit>> renewPermit = permitProvider.renewPermit(Arrays.asList(p));
                    if (renewPermit.size() > 0) {
                        Optional<Permit> optionalPermit = renewPermit.get(0);
                        if (optionalPermit.isPresent()) {
                            p = optionalPermit.get();
                            //System.out.println("Renewed:" + p);
                            permit.set(p);
                        } else {
                            //System.out.println("Lost:" + p);
                            permit.set(null);
                        }
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        public Optional<Integer> permitId() {
            Permit p = permit.get();
            if (p != null) {
                Optional<Permit> optionalPermit = permitProvider.isExpired(p);
                if (optionalPermit.isPresent()) {
                    p = optionalPermit.get();
                    return Optional.of(p.id);
                }
            }
            return Optional.absent();
        }

    }
}
