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
package com.jivesoftware.os.jive.utils.row.column.value.store.api.keys;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class SymetricalHashableKeyTest {

    Random rand = new Random();

    /**
     * Test of toHash and toBytes method, of class SymetricalHashableKey.
     */
    @Test
    public void testToHash() throws Exception {
        System.out.println("toHash");
        byte[] input = longBytes(123456L);
        SymetricalHashableKey instance = new SymetricalHashableKey("asdfghjk");
        byte[] hash = instance.toHash(input);
        byte[] bytes = instance.toBytes(hash);
        assertEquals(bytes, input);
    }

    @Test
    public void testThreadSafe() throws Exception {
        System.out.println("testThreadSafe");
        final SymetricalHashableKey instance = new SymetricalHashableKey("asdfghjk");
        int poolSize = 4;
        ExecutorService threadPool = Executors.newFixedThreadPool(poolSize);
        final CountDownLatch latch = new CountDownLatch(poolSize);
        final MutableBoolean failed = new MutableBoolean(false);
        for (int i = 0; i < poolSize; i++) {
            threadPool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        int count = 1000000;
                        for (long i = 0; i < count; i++) {
                            long a = Math.abs(rand.nextLong());
                            byte[] hash = instance.toHash(longBytes(a));
                            byte[] output = instance.toBytes(hash);
                            long b = bytesLong(output);
                            if (a != b) {
                                System.out.println("Failed a:" + a + " ne b:" + b);
                                failed.setValue(true);
                                return;
                            }
                        }
                    } catch (InvalidKeyException | ShortBufferException | IllegalBlockSizeException | BadPaddingException | ExecutionException x) {
                        failed.setValue(true);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        latch.await();
        Assert.assertFalse(failed.booleanValue());
    }

    @Test
    public void testOddsOfBeingInOrder() throws Exception {
        SymetricalHashableKey instance = new SymetricalHashableKey("asdfghjk");
        int count = 1000000;
        int ordered = 0;
        int unordered = 0;

        for (long i = 0; i < count; i++) {
            long a = Math.abs(rand.nextLong());
            long b = a + 1;
            long ha = Math.abs(longToHashAsLong(a, instance));
            long hb = Math.abs(longToHashAsLong(b, instance));
            if (ha < hb) {
                ordered++;
            } else {
                unordered++;
            }
        }

        System.out.println("rand vs rand+1");
        System.out.println("ordered:" + ordered + " vs unordered:" + unordered);

        for (long i = 0; i < count; i++) {
            long a = Math.abs(rand.nextLong());
            long b = Math.abs(rand.nextLong());
            long ha = Math.abs(longToHashAsLong(a, instance));
            long hb = Math.abs(longToHashAsLong(b, instance));
            if ((a < b) == (ha < hb)) {
                ordered++;
            } else {
                unordered++;
            }
        }
        System.out.println("rand vs rand");
        System.out.println("ordered:" + ordered + " vs unordered:" + unordered);
    }

    @Test
    public void testDistribution() throws Exception {
        System.out.println("distribution");
        SymetricalHashableKey instance = new SymetricalHashableKey("asdfghjk");

        int count = 1000000;
        long step = Long.MAX_VALUE / count;
        System.out.println("Step:" + step);
        int[] buckets = new int[32];
        for (long i = 0; i < Long.MAX_VALUE && i > -1; i += step) {
            long hashAsLong = longToHashAsLong(i, instance);
            double p = (double) hashAsLong / (double) Long.MAX_VALUE;
            int bi = (int) (buckets.length * p);
            buckets[bi]++;
        }

        for (int i = 0; i < buckets.length; i++) {
            System.out.println(i + " " + buckets[i]);
        }
    }

    //    @Test
    //    public void costTest() throws Exception {
    //        long start = System.currentTimeMillis();
    //        long count = 10000000L;
    //        SymetricalHashableKey instance = new SymetricalHashableKey("asdfghjk");
    //        for(long i=0;i<count;i++) {
    //            byte[] longBytes = longBytes(URandom.nextInt());
    //        }
    //        long overhead = System.currentTimeMillis()-start;
    //        double rawRate = ((double)count/(double)overhead);
    //        System.out.println("raw:"+rawRate+"/milli");
    //
    //        start = System.currentTimeMillis();
    //        for(long i=0;i<count;i++) {
    //            byte[] hash = instance.toHash(longBytes(URandom.nextInt()));
    //            byte[] bytes = instance.toBytes(hash);
    //        }
    //        long elapse = (System.currentTimeMillis()-start);
    //        double hashedRate = ((double)count/(double)elapse);
    //        System.out.println("hashed:"+hashedRate+"/milli");
    //
    //        // rawRate/100 = hasedRate/x
    //        System.out.println(100-(((hashedRate*100)/rawRate))+"% slower");
    //
    //    }
    private long longToHashAsLong(long i, SymetricalHashableKey instance) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException,
            ShortBufferException, ExecutionException {
        byte[] input = longBytes(i);
        byte[] hash = instance.toHash(input);
        long hashAsLong = Math.abs(bytesLong(hash));
        return hashAsLong;
    }

    public byte[] longBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(x);
        return buffer.array();
    }

    public long bytesLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(bytes);
        buffer.flip(); //need flip
        return buffer.getLong();
    }
}
