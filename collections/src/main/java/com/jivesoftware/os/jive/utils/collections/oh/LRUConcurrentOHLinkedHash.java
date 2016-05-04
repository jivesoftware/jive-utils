package com.jivesoftware.os.jive.utils.collections.oh;

import com.jivesoftware.os.jive.utils.collections.KeyValueStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author jonathan.colt
 */
public class LRUConcurrentOHLinkedHash<K, V> {

    private final int capacity;
    private final int maxCapacity;
    private final float slack;

    private final boolean hasValues;
    private final OHasher<K> hasher;
    private final OHEqualer<K> equaler;
    private final OHash<K, LRUValue<V>>[] hmaps;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile Thread cleaner;
    private final AtomicLong updates = new AtomicLong();
    private final AtomicLong syntheticTime = new AtomicLong(0);

    /**
     *
     * @param initialCapacity
     * @param maxCapacity
     * @param slack the percentage the size can go over the maxCapacity before the collections removes items (0.2 typical)
     * @param hasValues
     * @param concurrency
     */
    @SuppressWarnings("unchecked")
    public LRUConcurrentOHLinkedHash(int initialCapacity, int maxCapacity, float slack, boolean hasValues, int concurrency, OHasher<K> hasher,
        OHEqualer<K> equaler) {
        this.capacity = initialCapacity;
        this.maxCapacity = maxCapacity;
        this.slack = slack;
        this.hasValues = hasValues;
        this.hasher = hasher;
        this.equaler = equaler;
        this.hmaps = new OHash[concurrency];
    }

    public void put(K key, V value) {
        int hashCode = hasher.hashCode(key);
        OHash<K, LRUValue<V>> lhmap = lhmap(hashCode, true);
        LRUValue<V> v = new LRUValue<>(value, syntheticTime.incrementAndGet());
        synchronized (lhmap) {
            lhmap.remove(hashCode, key);
            lhmap.put(hashCode, key, v);
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

    private final FirstValueComparator<K, V> FIRST_VALUE_COMPARATOR = new FirstValueComparator<>();

    private final class FirstValueComparator<K, V> implements Comparator<FirstValue<K, V>> {

        @Override
        public int compare(FirstValue<K, V> o1, FirstValue<K, V> o2) {
            if (o1 == null && o2 == null) {
                return -1;
            } else if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            } else {
                return Long.compare(o1.timestamp, o2.timestamp);
            }
        }
    }

    public void cleanup() {
        int count = 0;
        for (OHash<K, LRUValue<V>> hmap : hmaps) {
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
                FirstValue<K,V>[] firstValues = new FirstValue[hmaps.length];
                for (int j = 0; j < hmaps.length; j++) {
                    OHash<K, LRUValue<V>> hmap = hmaps[j];
                    if (hmap != null) {
                        synchronized (hmap) {
                            LRUValue<V> firstValue = hmap.firstValue();
                            if (firstValue != null) {
                                firstValues[j] = new FirstValue(firstValue.timestamp, hmap);
                            }
                        }
                    }
                }
                while (removeCount > 0) {
                    Arrays.sort(firstValues, FIRST_VALUE_COMPARATOR);
                    if (firstValues[1] != null && firstValues[1].timestamp != Long.MAX_VALUE) {
                        while (firstValues[0].timestamp < firstValues[1].timestamp) {
                            firstValues[0].removeFirstValue();
                            removeCount--;
                        }
                    } else {
                        while (removeCount > 0) {
                            if (firstValues[0].hmap.removeFirstValue() == null) {
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

    private class FirstValue<K, V> {

        private long timestamp;
        private final OHash<K, LRUValue<V>> hmap;

        public FirstValue(long timestamp, OHash<K, LRUValue<V>> hmap) {
            this.timestamp = timestamp;
            this.hmap = hmap;
        }

        private void removeFirstValue() {
            synchronized (hmap) {
                LRUValue<V> removed = hmap.removeFirstValue();
                if (removed == null) {
                    timestamp = Long.MAX_VALUE;
                } else {
                    timestamp = removed.timestamp;
                }
            }
        }

    }

    private class LRUValue<V> {

        private final V value;
        private final long timestamp;

        private LRUValue(V value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }

    private OHash<K, LRUValue<V>> lhmap(int hashCode, boolean create) {
        int index = Math.abs((hashCode) % hmaps.length);
        if (hmaps[index] == null && create) {
            synchronized (hmaps) {
                if (hmaps[index] == null) {
                    hmaps[index] = new OHash<>(new OHLinkedMapState<>(capacity, hasValues, null), hasher, equaler);
                }
            }
        }
        return hmaps[index];
    }

    public V get(K key) {
        int hashCode = hasher.hashCode(key);
        OHash<K, LRUValue<V>> hmap = lhmap(hashCode, false);
        if (hmap != null) {
            LRUValue<V> got;
            synchronized (hmap) {
                got = hmap.get(hashCode, key);
            }
            if (got != null) {
                return got.value;
            }
        }
        return null;
    }

    public void remove(K key) {

        int hashCode = hasher.hashCode(key);
        OHash<K, LRUValue<V>> lhmap = lhmap(hashCode, false);
        if (lhmap != null) {
            synchronized (lhmap) {
                lhmap.remove(hashCode, key);
            }
        }
    }

    public void clear() {
        for (OHash<K, LRUValue<V>> lhmap : hmaps) {
            if (lhmap != null) {
                synchronized (lhmap) {
                    lhmap.clear();
                }
            }
        }
    }

    public int size() {
        int size = 0;
        for (OHash<K, LRUValue<V>> hmap : hmaps) {
            if (hmap != null) {
                size += hmap.size();
            }
        }
        return size;
    }

    public boolean stream(KeyValueStream<K, V> keyValueStream) throws Exception {
        for (OHash<K, LRUValue<V>> hmap : hmaps) {
            if (hmap != null) {
                if (!hmap.stream((K key, LRUValue<V> value) -> keyValueStream.keyValue(key, value.value))) {
                    return false;
                }
            }
        }
        return true;
    }

}
