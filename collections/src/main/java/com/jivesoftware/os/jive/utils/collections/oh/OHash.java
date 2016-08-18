package com.jivesoftware.os.jive.utils.collections.oh;

import com.jivesoftware.os.jive.utils.collections.KeyValueStream;

/**
 *
 * @author jonathan.colt
 */
public class OHash<K, V> implements OH<K, V> {

    private final OHasher<K> hasher;
    private final OHEqualer<K> equaler;
    private volatile OHState<K,V> state;

    /**
     *
     * @param capacity
     */
    public OHash(OHState<K, V> state, OHasher<K> hasher, OHEqualer<K> equaler) {
        this.hasher = hasher;
        this.equaler = equaler;
        this.state = state;
    }

    @Override
    public long size() {
        return state.size();
    }

    /**
     *
     * @return
     */
    @Override
    public void clear() {
        state = state.allocate(0);
    }

    private long hash(OHState state, long keyShuffle) {
        keyShuffle += keyShuffle >> 8; // shuffle bits to avoid worst case clustering

        if (keyShuffle < 0) {
            keyShuffle = -keyShuffle;
        }
        return keyShuffle % state.capacity();
    }

    V firstValue() {
        OHState<K, V> s = state;
        K skipped = s.skipped();
        long i = s.first();
        if (i != -1) {
            K key;
            key = s.key(i);
            if (key != null && key != skipped) {
                return s.value(i);
            }
        }
        return null;
    }

    V removeFirstValue() {
        OHState<K,V> s = state;
        K skipped = s.skipped();
        long i = s.first();
        if (i != -1) {
            K key;
            key = s.key(i);
            if (key != null && key != skipped) {
                V v = s.value(i);
                remove(key);
                return v;
            }
        }
        return null;
    }

    @Override
    public V get(K key) {
        return get(hasher.hashCode(key), key);
    }

    @Override
    public V get(long hashCode, K key) {
        OHState<K,V> s = state;
        K skipped = s.skipped();
        if (key == null || key == skipped) {
            return null;
        }
        if (s.size() == 0) {
            return null;
        }
        long capacity = s.capacity();
        long start = hash(s, hashCode);
        for (long i = start, j = 0, k = capacity; // stack vars for efficiency
            j < k; // max search for key
            i = (++i) % k, j++) { // wraps around table

            K storedKey = s.key(i);
            if (storedKey == skipped) {
                continue;
            }
            if (storedKey == null) {
                return null;
            }
            if (equaler.equals(storedKey, key)) {
                return s.value(i);
            }
        }
        return null;

    }

    @Override
    public void remove(K key) {
        remove(hasher.hashCode(key), key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(long hashCode, K key) {
        OHState<K,V> s = state;
        K skipped = s.skipped();
        if (key == null || key == skipped) {
            return;
        }
        if (s.size() == 0) {
            return;
        }
        long capacity = s.capacity();
        long start = hash(s, hashCode);
        for (long i = start, j = 0, k = capacity; // stack vars for efficiency
            j < k; // max search for key
            i = (++i) % k, j++) {					// wraps around table

            K storedKey = s.key(i);
            if (storedKey == skipped) {
                continue;
            }
            if (storedKey == null) {
                return;
            }

            if (equaler.equals(storedKey, key)) {
                s.remove(i, skipped, null);

                long next = (i + 1) % k;
                if (s.key(next) == null) {
                    for (long z = i, y = 0; y < capacity; z = (z + capacity - 1) % k, y++) {
                        if (s.key(z) != skipped) {
                            break;
                        }
                        s.clear(z);
                    }
                }
                return;
            }
        }
    }

    @Override
    public void put(K key,  V value) {
        put(hasher.hashCode(key), key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void put(long hashCode, K key, V value) {
        OHState<K,V> s = state;
        long capacity = s.capacity();
        if (s.size() * 2 >= capacity) {
            OHState<K,V> to = s.allocate(capacity * 2);
            rehash(s, to);
            state = to;
            s = to;
        }
        internalPut(s, hashCode, key,  value);
    }

    private void internalPut(OHState<K,V> s, long hashCode, K key, V value) {
        long capacity = s.capacity();
        long start = hash(s, hashCode);
        K skipped = s.skipped();
        for (long i = start, j = 0, k = capacity; // stack vars for efficiency
            j < k; // max search for available slot
            i = (++i) % k, j++) {
            // wraps around table

            K storedKey = s.key(i);
            if (storedKey == key) {
                s.update(i, key, value);
                return;
            }
            if (storedKey == null || storedKey == skipped) {
                s.link(i, key, value);
                return;
            }
            if (equaler.equals(storedKey, key)) {
                s.update(i, key, value);
                return;
            }
        }
    }

    private void rehash(OHState<K,V> from, OHState<K,V> to) {
        long i = from.first();
        K skipped = to.skipped();
        while (i != -1) {
            K storedKey = from.key(i);
            if (storedKey != null && storedKey != skipped) {
                long hash = hasher.hashCode(storedKey);
                internalPut(to, hash, storedKey, from.value(i));
            }
            i = from.next(i);
        }
    }

    @Override
    public boolean stream(KeyValueStream<K, V> stream) throws Exception {
        OHState<K,V> s = state;
        long c = s.capacity();
        if (c <= 0) {
            return true;
        }
        K skipped = s.skipped();
        long i = s.first();
        while (i != -1) {

            K key;
            V value = null;
            synchronized (this) {
                key = s.key(i);
                if (key != null && key != skipped) {
                    value = s.value(i);
                }
            }
            if (key != null && key != skipped) {
                if (!stream.keyValue(key, value)) {
                    return false;
                }
            }
            i = s.next(i);
        }
        return true;
    }

    @Override
    public String toString() {
        return "OHash{" + "hasher=" + hasher + ", equaler=" + equaler + ", state=" + state + '}';
    }

}
