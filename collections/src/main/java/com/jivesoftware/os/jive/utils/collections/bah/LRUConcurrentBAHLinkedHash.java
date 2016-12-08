package com.jivesoftware.os.jive.utils.collections.bah;

import com.jivesoftware.os.jive.utils.collections.KeyValueStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jonathan.colt
 */
public class LRUConcurrentBAHLinkedHash<V> {

    private final int capacity;
    private final int maxCapacity;
    private final float slack;

    private final boolean hasValues;
    private final BAHasher hasher;
    private final Semaphore[] hmapsSemaphore;
    private final BAHash<LRUValue<V>>[] hmaps;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile Thread cleaner;
    private final AtomicLong updates = new AtomicLong();
    private final AtomicLong syntheticTime = new AtomicLong(0);

    /**
     * @param initialCapacity
     * @param maxCapacity
     * @param slack           the percentage the size can go over the maxCapacity before the collections removes items (0.2 typical)
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
        this.hmapsSemaphore = new Semaphore[concurrency];
        this.hmaps = new BAHash[concurrency];
    }

    public void put(byte[] key, V value) throws InterruptedException {
        int hashCode = hasher.hashCode(key, 0, key.length);
        int i = lhmap(hashCode, true);
        BAHash<LRUValue<V>> hmap = hmaps[i];
        LRUValue<V> v = new LRUValue<>(value, syntheticTime.incrementAndGet());
        hmapsSemaphore[i].acquire(Short.MAX_VALUE);
        try {
            hmap.remove(hashCode, key, 0, key.length);
            hmap.put(hashCode, key, v);
        } finally {
            hmapsSemaphore[i].release(Short.MAX_VALUE);
        }
        if (updates.incrementAndGet() > maxCapacity * slack) {
            synchronized (updates) {
                updates.notifyAll();
            }
        }
    }

    public interface CleanerExceptionCallback {

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

    private static final FirstValueComparator FIRST_VALUE_COMPARATOR = new FirstValueComparator();

    private static final class FirstValueComparator implements Comparator<FirstValue> {

        @Override
        public int compare(FirstValue o1, FirstValue o2) {
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

    public void cleanup() throws InterruptedException {
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

                BAHash<LRUValue<V>> hmap = hmaps[0];
                if (hmap != null) {

                    while (removeCount > 0) {
                        hmapsSemaphore[0].acquire(Short.MAX_VALUE);
                        try {
                            if (hmap.removeFirstValue() == null) {
                                return;
                            }
                        } finally {
                            hmapsSemaphore[0].release(Short.MAX_VALUE);
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
                        hmapsSemaphore[j].acquire();
                        try {
                            LRUValue<V> firstValue = hmap.firstValue();
                            if (firstValue != null) {
                                firstValues[j] = new FirstValue<>(hmapsSemaphore[j], firstValue.timestamp, hmap);
                            }
                        } finally {
                            hmapsSemaphore[j].release();
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

    private static class FirstValue<V> {

        private final Semaphore semaphore;
        private long timestamp;
        private final BAHash<LRUValue<V>> hmap;

        public FirstValue(Semaphore semaphore, long timestamp, BAHash<LRUValue<V>> hmap) {
            this.semaphore = semaphore;
            this.timestamp = timestamp;
            this.hmap = hmap;
        }

        private void removeFirstValue() throws InterruptedException {
            semaphore.acquire(Short.MAX_VALUE);
            try {
                LRUValue<V> removed = hmap.removeFirstValue();
                if (removed == null) {
                    timestamp = Long.MAX_VALUE;
                } else {
                    timestamp = removed.timestamp;
                }
            } finally {
                semaphore.release(Short.MAX_VALUE);
            }
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

    private int lhmap(int hashCode, boolean create) {
        int index = Math.abs((hashCode) % hmaps.length);
        if (hmaps[index] == null && create) {
            synchronized (hmaps) {
                if (hmaps[index] == null) {
                    hmapsSemaphore[index] = new Semaphore(Short.MAX_VALUE, true);
                    hmaps[index] = new BAHash<>(new BAHLinkedMapState<>(capacity, hasValues, BAHMapState.NIL), hasher, BAHEqualer.SINGLETON);
                }
            }
        }
        return index;
    }

    public V get(byte[] key) throws InterruptedException {
        return get(key, 0, key.length);
    }

    public V get(byte[] key, int keyOffset, int keyLength) throws InterruptedException {
        int hashCode = hasher.hashCode(key, keyOffset, keyLength);
        int i = lhmap(hashCode, false);
        BAHash<LRUValue<V>> hmap = hmaps[i];
        if (hmap != null) {
            LRUValue<V> got;
            hmapsSemaphore[i].acquire();
            try {
                got = hmap.get(hashCode, key, keyOffset, keyLength);
            } finally {
                hmapsSemaphore[i].release();
            }
            if (got != null) {
                return got.value;
            }
        }
        return null;
    }

    public void remove(byte[] key) throws InterruptedException {
        remove(key, 0, key.length);
    }

    public void remove(byte[] key, int keyOffset, int keyLength) throws InterruptedException {
        int hashCode = hasher.hashCode(key, keyOffset, keyLength);
        int i = lhmap(hashCode, false);
        BAHash<LRUValue<V>> hmap = hmaps[i];
        if (hmap != null) {
            hmapsSemaphore[i].acquire(Short.MAX_VALUE);
            try {
                hmap.remove(hashCode, key, keyOffset, keyLength);
            } finally {
                hmapsSemaphore[i].release(Short.MAX_VALUE);
            }
        }
    }

    public void clear() throws InterruptedException {
        for (int i = 0; i < hmaps.length; i++) {
            BAHash<LRUValue<V>> hmap = hmaps[i];
            if (hmap != null) {
                hmapsSemaphore[i].acquire(Short.MAX_VALUE);
                try {
                    hmap.clear();
                } finally {
                    hmapsSemaphore[i].release(Short.MAX_VALUE);
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
        for (int i = 0; i < hmaps.length; i++) {
            BAHash<LRUValue<V>> hmap = hmaps[i];
            if (hmap != null) {
                if (!hmap.stream(hmapsSemaphore[i], (key, value) -> keyValueStream.keyValue(key, value.value))) {
                    return false;
                }
            }
        }
        return true;
    }

}
