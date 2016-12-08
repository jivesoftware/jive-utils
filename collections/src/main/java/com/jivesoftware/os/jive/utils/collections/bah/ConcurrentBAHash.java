package com.jivesoftware.os.jive.utils.collections.bah;

import com.jivesoftware.os.jive.utils.collections.KeyValueStream;
import java.util.concurrent.Semaphore;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author jonathan.colt
 */
public class ConcurrentBAHash<V> {

    private final int capacity;
    private final boolean hasValues;
    private final BAHasher hasher;
    private final Semaphore[] hmapsSemaphore;
    private final BAHash<V>[] hmaps;

    @SuppressWarnings("unchecked")
    public ConcurrentBAHash(int capacity, boolean hasValues, int concurrency) {
        this.capacity = capacity;
        this.hasValues = hasValues;
        this.hasher = BAHasher.SINGLETON;
        this.hmapsSemaphore = new Semaphore[concurrency];
        this.hmaps = new BAHash[concurrency];
    }

    public void put(byte[] key, V value) throws InterruptedException {
        int hashCode = hasher.hashCode(key, 0, key.length);
        int i = hmap(hashCode, true);
        BAHash<V> hmap = hmaps[i];
        hmapsSemaphore[i].acquire(Short.MAX_VALUE);
        try {
            hmap.put(hashCode, key, value);
        } finally {
            hmapsSemaphore[i].release(Short.MAX_VALUE);
        }
    }

    private int hmap(int hashCode, boolean create) {
        int index = Math.abs((hashCode) % hmaps.length);
        if (hmaps[index] == null && create) {
            synchronized (hmaps) {
                if (hmaps[index] == null) {
                    hmapsSemaphore[index] = new Semaphore(Short.MAX_VALUE, true);
                    hmaps[index] = new BAHash<>(new BAHMapState<>(capacity, hasValues, BAHMapState.NIL), hasher, BAHEqualer.SINGLETON);
                }
            }
        }
        return index;
    }

    public int hashCode(byte[] key, int offset, int length) {
        return hasher.hashCode(key, offset, length);
    }

    public V computeIfAbsent(byte[] key, Function<byte[], ? extends V> mappingFunction) throws InterruptedException {
        int hashCode = hasher.hashCode(key, 0, key.length);
        return computeIfAbsent(hashCode, key, mappingFunction);
    }

    public V computeIfAbsent(int hashCode, byte[] key, Function<byte[], ? extends V> mappingFunction) throws InterruptedException {
        int i = hmap(hashCode, true);
        BAHash<V> hmap = hmaps[i];
        hmapsSemaphore[i].acquire(Short.MAX_VALUE);
        try {
            V value = hmap.get(hashCode, key, 0, key.length);
            if (value == null) {
                value = mappingFunction.apply(key);
                hmap.put(hashCode, key, value);
            }
            return value;
        } finally {
            hmapsSemaphore[i].release(Short.MAX_VALUE);
        }
    }

    public V compute(byte[] key, BiFunction<byte[], ? super V, ? extends V> remappingFunction) throws InterruptedException {
        int hashCode = hasher.hashCode(key, 0, key.length);
        return compute(hashCode, key, remappingFunction);
    }

    public V compute(int hashCode, byte[] key, BiFunction<byte[], ? super V, ? extends V> remappingFunction) throws InterruptedException {
        int i = hmap(hashCode, true);
        BAHash<V> hmap = hmaps[i];
        hmapsSemaphore[i].acquire(Short.MAX_VALUE);
        try {
            V value = hmap.get(hashCode, key, 0, key.length);
            V remapped = remappingFunction.apply(key, value);
            if (remapped != value) {
                value = remapped;
                hmap.put(hashCode, key, value);
            }
            return value;
        } finally {
            hmapsSemaphore[i].release(Short.MAX_VALUE);
        }
    }

    public V get(byte[] key) throws InterruptedException {
        return get(key, 0, key.length);
    }

    public V get(byte[] key, int keyOffset, int keyLength) throws InterruptedException {
        int hashCode = hasher.hashCode(key, keyOffset, keyLength);
        return get(hashCode, key, keyOffset, keyLength);
    }

    public V get(int hashCode, byte[] key, int keyOffset, int keyLength) throws InterruptedException {
        int i = hmap(hashCode, false);
        BAHash<V> hmap = hmaps[i];
        if (hmap != null) {
            hmapsSemaphore[i].acquire();
            try {
                return hmap.get(hashCode, key, keyOffset, keyLength);
            } finally {
                hmapsSemaphore[i].release();
            }
        }
        return null;
    }

    public void remove(byte[] key) throws InterruptedException {
        remove(key, 0, key.length);
    }

    public void remove(byte[] key, int keyOffset, int keyLength) throws InterruptedException {
        int hashCode = hasher.hashCode(key, keyOffset, keyLength);
        remove(hashCode, key, keyOffset, keyLength);
    }

    public void remove(int hashCode, byte[] key, int keyOffset, int keyLength) throws InterruptedException {
        int i = hmap(hashCode, false);
        BAHash<V> hmap = hmaps[i];
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
            BAHash<V> hmap = hmaps[i];
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
        for (BAHash<V> hmap : hmaps) {
            if (hmap != null) {
                size += hmap.size();
            }
        }
        return size;
    }

    public boolean stream(KeyValueStream<byte[], V> keyValueStream) throws Exception {
        for (int i = 0; i < hmaps.length; i++) {
            BAHash<V> hmap = hmaps[i];
            if (hmap != null) {
                if (!hmap.stream(hmapsSemaphore[i], keyValueStream)) {
                    return false;
                }
            }
        }
        return true;
    }

}
