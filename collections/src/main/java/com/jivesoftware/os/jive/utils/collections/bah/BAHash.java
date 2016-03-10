package com.jivesoftware.os.jive.utils.collections.bah;

import com.jivesoftware.os.jive.utils.collections.KeyValueStream;

/**
 *
 * @author jonathan.colt
 */
public class BAHash<V> implements BAH<V> {

    private final BAHasher hasher;
    private final BAHEqualer equaler;
    private volatile BAHState<V> state;

    /**
     *
     * @param capacity
     */
    public BAHash(BAHState<V> state, BAHasher hasher, BAHEqualer equaler) {
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

    private long hash(BAHState state, long keyShuffle) {
        keyShuffle += keyShuffle >> 8; // shuffle bits to avoid worst case clustering

        if (keyShuffle < 0) {
            keyShuffle = -keyShuffle;
        }
        return keyShuffle % state.capacity();
    }

    V firstValue() {
        BAHState<V> s = state;
        byte[] skipped = s.skipped();
        long i = s.first();
        if (i != -1) {
            byte[] key;
            key = s.key(i);
            if (key != null && key != skipped) {
                return s.value(i);
            }
        }
        return null;
    }

    V removeFirstValue() {
        BAHState<V> s = state;
        byte[] skipped = s.skipped();
        long i = s.first();
        if (i != -1) {
            byte[] key;
            key = s.key(i);
            if (key != null && key != skipped) {
                V v = s.value(i);
                remove(key, 0, key.length);
                return v;
            }
        }
        return null;
    }

    @Override
    public V get(byte[] key, int keyOffset, int keyLength) {
        return get(hasher.hashCode(key, keyOffset, keyLength), key, keyOffset, keyLength);
    }

    @Override
    public V get(long hashCode, byte[] key, int keyOffset, int keyLength) {
        BAHState<V> s = state;
        byte[] skipped = s.skipped();
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

            byte[] storedKey = s.key(i);
            if (storedKey == skipped) {
                continue;
            }
            if (storedKey == null) {
                return null;
            }
            if (equaler.equals(storedKey, key, keyOffset, keyLength)) {
                return s.value(i);
            }
        }
        return null;

    }

    @Override
    public void remove(byte[] key, int keyOffset, int keyLength) {
        remove(hasher.hashCode(key, keyOffset, keyLength), key, keyOffset, keyLength);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove(long hashCode, byte[] key, int keyOffset, int keyLength) {
        BAHState<V> s = state;
        byte[] skipped = s.skipped();
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

            byte[] storedKey = s.key(i);
            if (storedKey == skipped) {
                continue;
            }
            if (storedKey == null) {
                return;
            }

            if (equaler.equals(storedKey, key, keyOffset, keyLength)) {
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
    public void put(byte[] key, int keyOffset, int keyLength, V value) {
        put(hasher.hashCode(key, keyOffset, keyLength), key, keyOffset, keyLength, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void put(long hashCode, byte[] key, int keyOffset, int keyLength, V value) {
        BAHState<V> s = state;
        long capacity = s.capacity();
        if (s.size() * 2 >= capacity) {
            BAHState<V> to = s.allocate(capacity * 2);
            rehash(s, to);
            state = to;
            s = to;
        }
        internalPut(s, hashCode, key, keyOffset, keyLength, value);
    }

    private void internalPut(BAHState<V> s, long hashCode, byte[] key, int keyOffset, int keyLength, V value) {
        long capacity = s.capacity();
        long start = hash(s, hashCode);
        byte[] skipped = s.skipped();
        for (long i = start, j = 0, k = capacity; // stack vars for efficiency
            j < k; // max search for available slot
            i = (++i) % k, j++) {
            // wraps around table

            byte[] storedKey = s.key(i);
            if (storedKey == key) {
                s.link(i, key, value);
                return;
            }
            if (storedKey == null || storedKey == skipped) {
                s.link(i, key, value);
                return;
            }
            if (equaler.equals(storedKey, key, keyOffset, keyLength)) {
                s.update(i, key, value);
                return;
            }
        }
    }

    private void rehash(BAHState<V> from, BAHState<V> to) {
        long i = from.first();
        byte[] skipped = to.skipped();
        while (i != -1) {
            byte[] storedKey = from.key(i);
            if (storedKey != null && storedKey != skipped) {
                long hash = hasher.hashCode(storedKey, 0, storedKey.length);
                internalPut(to, hash, storedKey, 0, storedKey.length, from.value(i));
            }
            i = from.next(i);
        }
    }

    @Override
    public boolean stream(KeyValueStream<byte[], V> stream) throws Exception {
        BAHState<V> s = state;
        long c = s.capacity();
        if (c <= 0) {
            return true;
        }
        byte[] skipped = s.skipped();
        long i = s.first();
        while (i != -1) {

            byte[] key;
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
        return "BAHash{" + "hasher=" + hasher + ", equaler=" + equaler + ", state=" + state + '}';
    }

}
