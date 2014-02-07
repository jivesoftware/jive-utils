package com.jivesoftware.os.jive.utils.map.store;

import com.jivesoftware.os.jive.utils.io.ByteBufferFactory;
import com.jivesoftware.os.jive.utils.map.store.extractors.ExtractIndex;
import com.jivesoftware.os.jive.utils.map.store.extractors.ExtractKey;
import com.jivesoftware.os.jive.utils.map.store.extractors.ExtractPayload;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Random;
import org.testng.annotations.Test;

/**
 *
 * @author jonathan
 */
public class MapStoreTest {

    @Test
    public void basicTest() {
        test();
    }

    /**
     *
     * @param _args
     */
    public static void main(final String[] _args) {
        for (int i = 0; i < 1; i++) {
            final int n = i;
            Thread t = new Thread() {

                @Override
                public void run() {
                    test();
                }
            };
            t.start();
        }
    }

    public static void test() {

        int it = 10000;
        int ksize = 4;
        test(it, ksize, it, new ByteBufferFactory() {

            @Override
            public ByteBuffer allocate(long _size) {
                return ByteBuffer.allocate((int) _size);
            }
        });
    }

    private static boolean test(int _iterations, int keySize, int _maxSize, ByteBufferFactory factory) {

        MapStore pset = new MapStore(new ExtractIndex(), new ExtractKey(), new ExtractPayload());
        int payloadSize = 4;

        System.out.println("Upper Bound Max Count = " + pset.absoluteMaxCount(keySize, payloadSize));
        MapChunk set = pset.allocate((byte) 0, (byte) 0, new byte[16], 0, _maxSize, keySize, payloadSize, factory);
        long seed = System.currentTimeMillis();
        int maxCapacity = pset.getCapacity(set);
        System.out.println("ByteSet size in mb for (" + _maxSize + ") is " + (set.size() / 1024d / 1024d) + "mb");

        Random random = new Random(seed);
        long t = System.currentTimeMillis();
        for (int i = 0; i < _iterations; i++) {
            try {
                pset.add(set, (byte) 1, randomLowerCaseAlphaBytes(random, keySize), intBytes(i));
            } catch (OverCapacityException x) {
                break;
            }
        }
        long elapse = System.currentTimeMillis() - t;
        System.out.println("ByteSet add(" + _iterations + ") took " + elapse + " " + pset.getCount(set));

        random = new Random(seed);
        t = System.currentTimeMillis();
        HashSet<String> jset = new HashSet<>(maxCapacity);
        for (int i = 0; i < _iterations; i++) {
            jset.add(new String(randomLowerCaseAlphaBytes(random, keySize)));
        }
        elapse = System.currentTimeMillis() - t;
        System.out.println("JavaHashSet add(" + _iterations + ") took " + elapse);

        random = new Random(seed);
        for (int i = 0; i < pset.getCount(set); i++) {
            byte[] got = pset.get(set, randomLowerCaseAlphaBytes(random, keySize), new ExtractPayload());
            assert got != null : "shouldn't be null";
            //int v = UIO.bytesInt(got);
            //assert v == i : "should be the same";
        }

        random = new Random(seed);
        t = System.currentTimeMillis();
        for (int i = 0; i < _iterations; i++) {
            try {
                pset.remove(set, randomLowerCaseAlphaBytes(random, keySize));
            } catch (Exception x) {
                x.printStackTrace();
                break;
            }
        }
        elapse = System.currentTimeMillis() - t;
        System.out.println("ByteSet remove(" + _iterations + ") took " + elapse + " " + pset.getCount(set));

        random = new Random(seed);
        t = System.currentTimeMillis();
        for (int i = 0; i < _iterations; i++) {
            try {
                jset.remove(new String(randomLowerCaseAlphaBytes(random, keySize)));
            } catch (Exception x) {
                x.printStackTrace();
                break;
            }
        }
        elapse = System.currentTimeMillis() - t;
        System.out.println("JavaHashSet remove(" + _iterations + ") took " + elapse);

        random = new Random(seed);
        t = System.currentTimeMillis();
        for (int i = 0; i < _maxSize; i++) {
            if (i % 2 == 0) {
                pset.remove(set, randomLowerCaseAlphaBytes(random, keySize));
            } else {
                pset.add(set, (byte) 1, randomLowerCaseAlphaBytes(random, keySize), intBytes(i));
            }
        }
        elapse = System.currentTimeMillis() - t;
        System.out.println("ByteSet add and remove (" + _maxSize + ") took " + elapse + " " + pset.getCount(set));

        random = new Random(seed);
        t = System.currentTimeMillis();
        for (int i = 0; i < _maxSize; i++) {
            if (i % 2 == 0) {
                jset.remove(new String(randomLowerCaseAlphaBytes(random, keySize)));
            } else {
                jset.add(new String(randomLowerCaseAlphaBytes(random, keySize)));
            }
        }
        elapse = System.currentTimeMillis() - t;
        System.out.println("JavaHashSet add and remove (" + _maxSize + ") took " + elapse);

        random = new Random(seed);
        t = System.currentTimeMillis();
        for (int i = 0; i < _maxSize; i++) {
            pset.contains(set, randomLowerCaseAlphaBytes(random, keySize));
        }
        elapse = System.currentTimeMillis() - t;
        System.out.println("ByteSet contains (" + _maxSize + ") took " + elapse + " " + pset.getCount(set));

        random = new Random(seed);
        t = System.currentTimeMillis();
        for (int i = 0; i < _maxSize; i++) {
            jset.contains(new String(randomLowerCaseAlphaBytes(random, keySize)));
        }
        elapse = System.currentTimeMillis() - t;
        System.out.println("JavaHashSet contains (" + _maxSize + ") took " + elapse);

        random = new Random(seed);
        for (int i = 0; i < _maxSize; i++) {
            if (i % 2 == 0) {
                randomLowerCaseAlphaBytes(random, keySize);
            } else {
                pset.get(set, randomLowerCaseAlphaBytes(random, keySize), new ExtractPayload());
                //assert got == i;
            }
        }
        System.out.println("count " + pset.getCount(set));

        return true;
    }

    static byte[] randomLowerCaseAlphaBytes(Random random, int _length) {
        byte[] name = new byte[_length];
        fill(random, name, 0, _length, 97, 122); // 97 122 lowercase a to z ascii
        return name;
    }

    static void fill(Random random, byte[] _fill, int _offset, int _length, int _min, int _max) {
        for (int i = _offset; i < _offset + _length; i++) {
            _fill[i] = (byte) (_min + random.nextInt(_max - _min));
        }
    }

    static byte[] intBytes(int v) {
        return intBytes(v, new byte[4], 0);
    }

    static byte[] intBytes(int v, byte[] _bytes, int _offset) {
        _bytes[_offset + 0] = (byte) (v >>> 24);
        _bytes[_offset + 1] = (byte) (v >>> 16);
        _bytes[_offset + 2] = (byte) (v >>> 8);
        _bytes[_offset + 3] = (byte) v;
        return _bytes;
    }

}
