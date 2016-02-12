package com.jivesoftware.os.jive.utils.collections.lh;

/**
 *
 * @author jonathan.colt
 */
public class ConcurrentLHash<V> {

    private final long capacity;
    private final long nilKey;
    private final long skipKey;

    private final LHash<V>[] maps;

    @SuppressWarnings("unchecked")
    public ConcurrentLHash(long capacity, long nilKey, long skipKey, int concurrency) {
        this.capacity = capacity;
        this.nilKey = nilKey;
        this.skipKey = skipKey;
        this.maps = new LHash[concurrency];
    }

    public void put(long key, V value) {
        LHash<V> hmap = hmap(key, true);
        synchronized (hmap) {
            hmap.put(key, value);
        }
    }

    private LHash<V> hmap(long key, boolean create) {
        int index = Math.abs((Long.hashCode(key)) % maps.length);
        if (maps[index] == null && create) {
            synchronized (maps) {
                if (maps[index] == null) {
                    maps[index] = new LHash<>(new LHMapState<>(capacity, nilKey, skipKey));
                }
            }
        }
        return maps[index];
    }

    public V get(long key) {
        LHash<V> hmap = hmap(key, false);
        if (hmap != null) {
            synchronized (hmap) {
                return hmap.get(key);
            }
        }
        return null;
    }

    public void remove(long key) {
        LHash<V> hmap = hmap(key, false);
        if (hmap != null) {
            synchronized (hmap) {
                hmap.remove(key);
            }
        }
    }

    public void clear() {
        for (LHash<V> hmap : maps) {
            if (hmap != null) {

                synchronized (hmap) {
                    hmap.clear();
                }
            }
        }
    }

    public int size() {
        int size = 0;
        for (LHash<V> hmap : maps) {
            if (hmap != null) {
                size += hmap.size();
            }
        }
        return size;
    }

}
