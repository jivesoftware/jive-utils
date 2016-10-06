package com.jivesoftware.os.jive.utils.collections.bah;

import com.jivesoftware.os.jive.utils.collections.baph.BAPHEqualer;
import com.jivesoftware.os.jive.utils.collections.baph.BAPHMapState;
import com.jivesoftware.os.jive.utils.collections.baph.BAPHash;
import com.jivesoftware.os.jive.utils.collections.baph.BAPHasher;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Random;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author jonathan.colt
 */
public class BAPHashNGTest {


    @Test
    public void testPut() throws Exception {
        SimpleBAPReader reader = new SimpleBAPReader();
        BAPHash<String> map = new BAPHash<>(new BAPHMapState<String>(10, true, BAPHMapState.NIL_POINTER, BAPHMapState.NIL, reader), BAPHasher.SINGLETON,
            BAPHEqualer.SINGLETON);
        internalTestPuts(reader, map);
    }

    private void internalTestPuts(SimpleBAPReader reader, BAPHash<String> map) throws Exception {
        int count = 64;
        for (byte i = 0; i < count; i++) {
            byte[] key = new byte[]{i};
            long pointer = reader.allocate(key);
            map.put(pointer, key, String.valueOf(i));
        }

        map.stream((byte[] key, String value) -> {
            System.out.println(Arrays.toString(key) + "->" + value);
            return true;
        });

        for (byte i = 0; i < count; i++) {
            Assert.assertEquals(map.get(new byte[]{i}, 0, 1), String.valueOf(i));
        }
    }

    @Test
    public void testRemove() throws Exception {
        Random r = new Random();
        SimpleBAPReader reader = new SimpleBAPReader();
        BAPHash<String> map = new BAPHash<>(new BAPHMapState<String>(10, true, BAPHMapState.NIL_POINTER, BAPHMapState.NIL, reader), BAPHasher.SINGLETON,
            BAPHEqualer.SINGLETON);
        internalTestRemoves(reader, map, r, false);
    }

    private void internalTestRemoves(SimpleBAPReader reader, BAPHash<String> map, Random r, boolean assertOrder) throws Exception {

        LinkedHashMap<ByteArrayKey, String> validation = new LinkedHashMap<>();

        int count = 7;

        // Add all
        for (byte i = 0; i < count; i++) {
            byte[] key = new byte[]{i};
            long pointer = reader.allocate(key);
            map.remove(key, 0, 1);
            validation.remove(new ByteArrayKey(new byte[]{i}));
        }

        for (byte i = 0; i < count; i++) {
            byte[] key = new byte[]{i};
            long pointer = reader.allocate(key);
            map.put(pointer, key, String.valueOf(i));
            validation.put(new ByteArrayKey(new byte[]{i}), String.valueOf(i));
        }

        if (assertOrder) {
            assertOrder("1 ", validation, map);
        }

        // Remove ~ half
        byte[][] retained = new byte[count][];
        byte[][] removed = new byte[count][];
        for (byte i = 0; i < count; i++) {
            byte[] key = new byte[]{i};
            if (r.nextBoolean()) {
                System.out.println("Removed:" + i);
                map.remove(key, 0, 1);
                validation.remove(new ByteArrayKey(new byte[]{i}));
                removed[i] = key;
            } else {
                retained[i] = key;
            }
        }

        if (assertOrder) {
            assertOrder("2 ", validation, map);
        }

        for (byte[] bs : retained) {
            if (bs != null) {
                Assert.assertEquals(map.get(bs, 0, 1), String.valueOf(bs[0]));
            }
        }

        for (byte[] bs : removed) {
            if (bs != null) {
                Assert.assertEquals(map.get(bs, 0, 1), null);
            }
        }

        // Add all back
        for (byte i = 0; i < count; i++) {
            byte[] key = new byte[]{i};
            long pointer = reader.allocate(key);
            map.put(pointer, key, String.valueOf(i));
            validation.put(new ByteArrayKey(new byte[]{i}), String.valueOf(i));
        }

        if (assertOrder) {
            assertOrder("3 ", validation, map);
        }

        // Remove ~ half
        retained = new byte[count][];
        removed = new byte[count][];
        for (byte i = 0; i < count; i++) {
            byte[] key = new byte[]{i};
            if (r.nextBoolean()) {
                System.out.println("Removed:" + i);
                map.remove(key, 0, 1);
                validation.remove(new ByteArrayKey(new byte[]{i}));
                removed[i] = key;
            } else {
                retained[i] = key;
            }
        }

        if (assertOrder) {
            assertOrder("4 ", validation, map);
        }

        for (byte[] bs : retained) {
            if (bs != null) {
                Assert.assertEquals(map.get(bs, 0, 1), String.valueOf(bs[0]));
            }
        }

        for (byte[] bs : removed) {
            if (bs != null) {
                Assert.assertEquals(map.get(bs, 0, 1), null);
            }
        }

        // Add all back
        for (byte i = 0; i < count; i++) {
            byte[] key = new byte[]{i};
            long pointer = reader.allocate(key);
            map.put(pointer, key, String.valueOf(i));
            validation.put(new ByteArrayKey(new byte[]{i}), String.valueOf(i));
        }

        if (assertOrder) {
            assertOrder("5 ", validation, map);
        }

        // Remove all in reverse order
        for (byte i = (byte) count; i > -1; i--) {
            validation.remove(new ByteArrayKey(new byte[]{i}));
            map.remove(new byte[]{i}, 0, 1);
        }

        if (assertOrder) {
            assertOrder("6 ", validation, map);
        }

        for (byte i = 0; i < count; i++) {
            Assert.assertNull(map.get(new byte[]{i}, 0, 1));
        }
    }

    private void assertOrder(String step, LinkedHashMap<ByteArrayKey, String> validation, BAPHash<String> map) throws Exception {
        ByteArrayKey[] expectedOrder = validation.keySet().toArray(new ByteArrayKey[0]);
        int[] i = new int[]{0};
        try {
            map.stream((byte[] key, String value) -> {
                Assert.assertTrue(Arrays.equals(key, expectedOrder[i[0]].key), Arrays.toString(key) + " vs " + Arrays.toString(expectedOrder[i[0]].key));
                i[0]++;
                return true;
            });
            Assert.assertEquals(i[0], expectedOrder.length);
        } catch (Throwable x) {
            for (ByteArrayKey byteArrayKey : expectedOrder) {
                System.out.println(step + " Expected:" + Arrays.toString(byteArrayKey.key));
            }
            map.stream((byte[] key, String value) -> {
                System.out.println(step + "Was:" + Arrays.toString(key));
                return true;
            });
            throw x;
        }

    }

    private static class ByteArrayKey {

        private final byte[] key;

        public ByteArrayKey(byte[] key) {
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ByteArrayKey iba = (ByteArrayKey) o;

            return Arrays.equals(key, iba.key);

        }

        @Override
        public int hashCode() {
            return key != null ? Arrays.hashCode(key) : 0;
        }
    }
}
