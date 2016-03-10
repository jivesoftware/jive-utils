package com.jivesoftware.os.jive.utils.collections.bah;

import com.jivesoftware.os.jive.utils.collections.KeyValueStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author jonathan.colt
 */
public class LRUConcurrentBAHLinkedHash<V> {

    private final int capacity;
    private final int maxCapacity;
    private final float slack;

    private final boolean hasValues;
    private final BAHasher hasher;
    private final BAHash<LRUValue<V>>[] hmaps;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile Thread cleaner;
    private final AtomicLong updates = new AtomicLong();
    private final AtomicLong syntheticTime = new  AtomicLong(0);

    /**
     *
     * @param initialCapacity
     * @param maxCapacity
     * @param slack the percentage the size can go over the maxCapacity before the collections removes items (0.2 typical)
     * @param hasValues
     * @param concurrency
     */
    @SuppressWarnings("unchecked")
    public LRUConcurrentBAHLinkedHash(int initialCapacity, int maxCapacity, float slack, boolean hasValues, int concurrency) {
        this.capacity = initialCapacity;
        this.maxCapacity = maxCapacity;
        this.slack = slack;
        this.hasValues = hasValues;
        this.hasher = BAHasher.SINGLETON;
        this.hmaps = new BAHash[concurrency];
    }

    public void put(byte[] key, V value) {
        put(key, 0, key.length, value);
    }

    private void put(byte[] key, int keyOffset, int keyLength, V value) {
        int hashCode = hasher.hashCode(key, keyOffset, keyLength);
        BAHash<LRUValue<V>> lhmap = lhmap(hashCode, true);
        LRUValue<V> v = new LRUValue<>(value, syntheticTime.incrementAndGet());
        synchronized (lhmap) {
            lhmap.remove(hashCode, key, keyOffset, keyLength);
            lhmap.put(hashCode, key, keyOffset, keyLength, v);
        }
        if (updates.incrementAndGet() > maxCapacity * slack) {
            synchronized (updates) {
                updates.notifyAll();
            }
        }
    }

    public static interface CleanerExceptionCallback {

        boolean exception(Throwable t);
    }

    public void start(String name, long cleanupIntervalInMillis, CleanerExceptionCallback cleanerExceptionCallback) {
        if (running.compareAndSet(false, true)) {
            new Thread(() -> {
                while (running.get()) {
                    try {
                        if (updates.get() < maxCapacity * slack) {
                            synchronized (updates) {
                                updates.wait(cleanupIntervalInMillis);
                            }
                        }
                        updates.set(0);
                        cleanup();
                    } catch (Exception t) {
                        if (cleanerExceptionCallback.exception(t)) {
                            running.set(false);
                        }
                    }
                }
            }, "LRUConcurrentBAHLinkedHash-cleaner-" + name).start();
        }
    }

    public void cleanup() {
        int count = 0;
        for (BAHash<LRUValue<V>> hmap : hmaps) {
            if (hmap != null) {
                count += hmap.size();
            }
        }
        int remainingCapacity = maxCapacity - (int) (count * (1f - slack));
        if (remainingCapacity < 0) {
            int removeCount = count - maxCapacity;
            if (hmaps.length == 1) {
                if (hmaps[0] != null) {
                    while (removeCount > 0) {
                        if (hmaps[0].removeFirstValue() == null) {
                            return;
                        }
                        removeCount--;
                    }
                }
            } else {
                @SuppressWarnings("unchecked")
                FirstValue<V>[] firstValues = new FirstValue[hmaps.length];
                for (int j = 0; j < hmaps.length; j++) {
                    BAHash<LRUValue<V>> hmap = hmaps[j];
                    if (hmap != null) {
                        synchronized (hmap) {
                            LRUValue<V> firstValue = hmap.firstValue();
                            if (firstValue != null) {
                                firstValues[j] = new FirstValue<>(firstValue.timestamp, hmap);
                            }
                        }
                    }
                }
                while (removeCount > 0) {
                    Arrays.sort(firstValues);
                    if (firstValues[1] != null) {
                        while (firstValues[0].timestamp < firstValues[1].timestamp) {
                            firstValues[0].removeFirstValue();
                            removeCount--;
                        }
                    } else {
                        while (removeCount > 0) {
                            if (hmaps[0].removeFirstValue() == null) {
                                return;
                            }
                            removeCount--;
                        }
                    }
                }
            }
        }
    }

    public void stop() {
        running.set(false);
        Thread t = cleaner;
        if (t != null) {
            if (!t.isInterrupted()) {
                t.interrupt();
            }
            cleaner = null;
        }
    }

    private static class FirstValue<V> implements Comparable<FirstValue<V>> {

        private long timestamp;
        private final BAHash<LRUValue<V>> hmap;

        public FirstValue(long timestamp, BAHash<LRUValue<V>> hmap) {
            this.timestamp = timestamp;
            this.hmap = hmap;
        }

        private void removeFirstValue() {
            synchronized (hmap) {
                LRUValue<V> removed = hmap.removeFirstValue();
                if (removed == null) {
                    timestamp = -1;
                } else {
                    timestamp = removed.timestamp;
                }
            }
        }

        @Override
        public int compareTo(FirstValue<V> o) {
            if (o == null) {
                return 1;
            }
            int c = Long.compare(timestamp, o.timestamp);
            if (c != 0) {
                return c;
            }
            if (hmap == null || o.hmap == null) { // drive nulls to the bottom
                return hmap == o.hmap ? 0 : 1;
            }
            return c;
        }

    }

    private static class LRUValue<V> {

        private final V value;
        private final long timestamp;

        private LRUValue(V value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }

    private BAHash<LRUValue<V>> lhmap(int hashCode, boolean create) {
        int index = Math.abs((hashCode) % hmaps.length);
        if (hmaps[index] == null && create) {
            synchronized (hmaps) {
                if (hmaps[index] == null) {
                    hmaps[index] = new BAHash<>(new BAHLinkedMapState<>(capacity, hasValues, BAHMapState.NIL), hasher, BAHEqualer.SINGLETON);
                }
            }
        }
        return hmaps[index];
    }

    public V get(byte[] key) {
        return get(key, 0, key.length);
    }

    public V get(byte[] key, int keyOffset, int keyLength) {
        int hashCode = hasher.hashCode(key, keyOffset, keyLength);
        BAHash<LRUValue<V>> hmap = lhmap(hashCode, false);
        if (hmap != null) {
            LRUValue<V> got;
            synchronized (hmap) {
                got = hmap.get(hashCode, key, keyOffset, keyLength);
            }
            if (got != null) {
                return got.value;
            }
        }
        return null;
    }

    public void remove(byte[] key) {
        remove(key, 0, key.length);
    }

    public void remove(byte[] key, int keyOffset, int keyLength) {
        int hashCode = hasher.hashCode(key, keyOffset, keyLength);
        BAHash<LRUValue<V>> lhmap = lhmap(hashCode, false);
        if (lhmap != null) {
            synchronized (lhmap) {
                lhmap.remove(hashCode, key, keyOffset, keyLength);
            }
        }
    }

    public void clear() {
        for (BAHash<LRUValue<V>> lhmap : hmaps) {
            if (lhmap != null) {
                synchronized (lhmap) {
                    lhmap.clear();
                }
            }
        }
    }

    public int size() {
        int size = 0;
        for (BAHash<LRUValue<V>> hmap : hmaps) {
            if (hmap != null) {
                size += hmap.size();
            }
        }
        return size;
    }

    public boolean stream(KeyValueStream<byte[], V> keyValueStream) throws Exception {
        for (BAHash<LRUValue<V>> hmap : hmaps) {
            if (hmap != null) {
                if (!hmap.stream((byte[] key, LRUValue<V> value) -> keyValueStream.keyValue(key, value.value))) {
                    return false;
                }
            }
        }
        return true;
    }

}