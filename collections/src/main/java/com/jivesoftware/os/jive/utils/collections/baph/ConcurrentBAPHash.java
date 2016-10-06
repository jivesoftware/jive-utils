package com.jivesoftware.os.jive.utils.collections.baph;

import com.jivesoftware.os.jive.utils.collections.KeyValueStream;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 *
 * @author jonathan.colt
 */
public class ConcurrentBAPHash<V> {

    private final int capacity;
    private final boolean hasValues;
    private final BAPHasher hasher;
    private final BAPHash<V>[] hmaps;
    private final BAPReader reader;

    @SuppressWarnings("unchecked")
    public ConcurrentBAPHash(int capacity, boolean hasValues, int concurrency, BAPReader reader) {
        this.capacity = capacity;
        this.hasValues = hasValues;
        this.hasher = BAPHasher.SINGLETON;
        this.hmaps = new BAPHash[concurrency];
        this.reader = reader;
    }

    public void put(long keyPointer, byte[] key, V value) {
        int hashCode = hasher.hashCode(key, 0, key.length);
        BAPHash<V> hmap = hmap(hashCode, true);
        synchronized (hmap) {
            hmap.put(hashCode, keyPointer, key, value);
        }
    }

    private BAPHash<V> hmap(int hashCode, boolean create) {
        int index = Math.abs((hashCode) % hmaps.length);
        if (hmaps[index] == null && create) {
            synchronized (hmaps) {
                if (hmaps[index] == null) {
                    hmaps[index] = new BAPHash<>(new BAPHMapState<>(capacity, hasValues, BAPHMapState.NIL_POINTER, BAPHMapState.NIL, reader), hasher,
                        BAPHEqualer.SINGLETON);
                }
            }
        }
        return hmaps[index];
    }

    public int hashCode(byte[] key, int offset, int length) {
        return hasher.hashCode(key, offset, length);
    }

    public V computeIfAbsent(long keyPointer, byte[] key, Function<byte[], ? extends V> mappingFunction) {
        int hashCode = hasher.hashCode(key, 0, key.length);
        return computeIfAbsent(hashCode, keyPointer, key, mappingFunction);
    }

    public V computeIfAbsent(int hashCode, long keyPointer, byte[] key, Function<byte[], ? extends V> mappingFunction) {
        BAPHash< V> hmap = hmap(hashCode, true);
        synchronized (hmap) {
            V value = hmap.get(hashCode, key, 0, key.length);
            if (value == null) {
                value = mappingFunction.apply(key);
                hmap.put(hashCode, key, value);
            }
            return value;
        }
    }

    public V compute(byte[] key, BiFunction<byte[], ? super V, ? extends V> remappingFunction) {
        int hashCode = hasher.hashCode(key, 0, key.length);
        return compute(hashCode, key, remappingFunction);
    }

    public V compute(int hashCode, byte[] key, BiFunction<byte[], ? super V, ? extends V> remappingFunction) {
        BAPHash<V> hmap = hmap(hashCode, true);
        synchronized (hmap) {
            V value = hmap.get(hashCode, key, 0, key.length);
            V remapped = remappingFunction.apply(key, value);
            if (remapped != value) {
                value = remapped;
                hmap.put(hashCode, key, value);
            }
            return value;
        }
    }

    public V get(byte[] key) {
        return get(key, 0, key.length);
    }

    public V get(byte[] key, int keyOffset, int keyLength) {
        int hashCode = hasher.hashCode(key, keyOffset, keyLength);
        return get(hashCode, key, keyOffset, keyLength);
    }

    public V get(int hashCode, byte[] key, int keyOffset, int keyLength) {
        BAPHash<V> hmap = hmap(hashCode, false);
        if (hmap != null) {
            synchronized (hmap) {
                return hmap.get(hashCode, key, keyOffset, keyLength);
            }
        }
        return null;
    }

    public void remove(byte[] key) {
        remove(key, 0, key.length);
    }

    public void remove(byte[] key, int keyOffset, int keyLength) {
        int hashCode = hasher.hashCode(key, keyOffset, keyLength);
        remove(hashCode, key, keyOffset, keyLength);
    }

    public void remove(int hashCode, byte[] key, int keyOffset, int keyLength) {
        BAPHash< V> hmap = hmap(hashCode, false);
        if (hmap != null) {
            synchronized (hmap) {
                hmap.remove(hashCode, key, keyOffset, keyLength);
            }
        }
    }

    public void clear() {
        for (BAPHash< V> hmap : hmaps) {
            if (hmap != null) {
                synchronized (hmap) {
                    hmap.clear();
                }
            }
        }
    }

    public int size() {
        int size = 0;
        for (BAPHash<V> hmap : hmaps) {
            if (hmap != null) {
                size += hmap.size();
            }
        }
        return size;
    }

    public boolean stream(KeyValueStream<byte[], V> keyValueStream) throws Exception {
        for (BAPHash<V> hmap : hmaps) {
            if (hmap != null) {
                if (!hmap.stream(keyValueStream)) {
                    return false;
                }
            }
        }
        return true;
    }

}
