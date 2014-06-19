/*
 * Copyright 2013 Jive Software, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.jivesoftware.os.jive.utils.ordered.id;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
        OrderIdProvider orderIdProvider = new OrderIdProviderImpl(new ConstantWriterIdProvider(1));
        long firstOrderId = orderIdProvider.nextId();
        long secondOrderId = orderIdProvider.nextId();
        int compare = (firstOrderId < secondOrderId) ? -1 : ((firstOrderId == secondOrderId) ? 0 : 1);
        Assert.assertTrue(compare < 0);
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
        new OrderIdProviderImpl(new ConstantWriterIdProvider(-1)).nextId();
    }

    @SuppressWarnings ("unused")
    @Test (expectedExceptions = IllegalArgumentException.class)
    public void testWriterIdOverBounds() {
        new OrderIdProviderImpl(new ConstantWriterIdProvider(1024)).nextId();
    }

    @Test (groups = "slow")
    public void testMultiThreadedUniqueIds() throws InterruptedException {
        final Set<Long> generated = new ConcurrentSkipListSet<>();
        final OrderIdProviderImpl provider = new OrderIdProviderImpl(new ConstantWriterIdProvider(2));
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
