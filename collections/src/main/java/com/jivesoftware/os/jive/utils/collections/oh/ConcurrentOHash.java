package com.jivesoftware.os.jive.utils.collections.oh;

import com.jivesoftware.os.jive.utils.collections.KeyValueStream;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 * @author jonathan.colt
 */
public class ConcurrentOHash<K, V> {

    private final int capacity;
    private final boolean hasValues;
    private final OHasher<K> hasher;
    private final OHEqualer<K> equaler;

    private final OHash<K, V>[] hmaps;

    @SuppressWarnings("unchecked")
    public ConcurrentOHash(int capacity, boolean hasValues, int concurrency, OHasher<K> hasher, OHEqualer<K> equaler) {
        this.capacity = capacity;
        this.hasValues = hasValues;
        this.hasher = hasher;
        this.equaler = equaler;
        this.hmaps = new OHash[concurrency];
    }

    public void put(K key, V value) {
        int hashCode = hasher.hashCode(key);
        OHash<K, V> hmap = hmap(hashCode, true);
        synchronized (hmap) {
            hmap.put(hashCode, key, value);
        }
    }

    private OHash<K, V> hmap(int hashCode, boolean create) {
        int index = Math.abs((hashCode) % hmaps.length);
        if (hmaps[index] == null && create) {
            synchronized (hmaps) {
                if (hmaps[index] == null) {
                    hmaps[index] = new OHash<>(new OHMapState<>(capacity, hasValues, null), hasher, equaler);
                }
            }
        }
        return hmaps[index];
    }

    public V computeIfAbsent(K key, Function<K, ? extends V> mappingFunction) {
        int hashCode = hasher.hashCode(key);
        OHash<K, V> hmap = hmap(hashCode, true);
        synchronized (hmap) {
            V value = hmap.get(hashCode, key);
            if (value == null) {
                value = mappingFunction.apply(key);
                hmap.put(hashCode, key, value);
            }
            return value;
        }
    }

    public V compute(K key, BiFunction<K, ? super V, ? extends V> remappingFunction) {
        int hashCode = hasher.hashCode(key);
        OHash<K, V> hmap = hmap(hashCode, true);
        synchronized (hmap) {
            V value = hmap.get(hashCode, key);
            V remapped = remappingFunction.apply(key, value);
            if (remapped != value) {
                value = remapped;
                hmap.put(hashCode, key, value);
            }
            return value;
        }
    }

    public V get(K key) {
        int hashCode = hasher.hashCode(key);
        OHash<K, V> hmap = hmap(hashCode, false);
        if (hmap != null) {
            synchronized (hmap) {
                return hmap.get(hashCode, key);
            }
        }
        return null;
    }

    public void remove(K key) {
        int hashCode = hasher.hashCode(key);
        OHash<K, V> hmap = hmap(hashCode, false);
        if (hmap != null) {
            synchronized (hmap) {
                hmap.remove(hashCode, key);
            }
        }
    }

    public void clear() {
        for (OHash<K, V> hmap : hmaps) {
            if (hmap != null) {
                synchronized (hmap) {
                    hmap.clear();
                }
            }
        }
    }

    public int size() {
        int size = 0;
        for (OHash<K, V> hmap : hmaps) {
            if (hmap != null) {
                size += hmap.size();
            }
        }
        return size;
    }

    public boolean stream(KeyValueStream<K, V> keyValueStream) throws Exception {
        for (OHash<K, V> hmap : hmaps) {
            if (hmap != null) {
                if (!hmap.stream(keyValueStream)) {
                    return false;
                }
            }
        }
        return true;
    }

}
