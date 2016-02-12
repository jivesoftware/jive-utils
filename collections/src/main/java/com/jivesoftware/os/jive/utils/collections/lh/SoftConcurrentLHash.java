package com.jivesoftware.os.jive.utils.collections.lh;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

/**
 *
 * @author jonathan.colt
 */
public class SoftConcurrentLHash<V> {

    //private final TLongObjectHashMap<V>[] maps;
    private final LHash<V>[] maps;
    private final ReferenceQueue<V> referenceQueue = new ReferenceQueue<>();

    @SuppressWarnings("unchecked")
    public SoftConcurrentLHash(long capacity, long nilKey, long skipKey, int concurrency) {
        this.maps = new LHash[concurrency];
        for (int i = 0; i < concurrency; i++) {
            this.maps[i] = new LHash<>(new LHMapState<>(null, capacity, nilKey, skipKey));
        }
    }

    public void put(long key, V value) {
        LHash<V> hmap = hmap(key);
        SoftReference<V> softValue = new SoftReference<>(value);
        synchronized (hmap) {
            hmap.put(key, value);
        }
    }

    private LHash<V> hmap(long key) {
        return maps[Math.abs((Long.hashCode(key)) % maps.length)];
    }

    public V get(long key) {
        LHash<V> hmap = hmap(key);
        synchronized (hmap) {
            return hmap.get(key);
        }
    }

    public void remove(long key) {
        LHash<V> hmap = hmap(key);
        synchronized (hmap) {
            hmap.remove(key);
        }
    }

    public void clear() {
        for (LHash<V> hmap : maps) {
            synchronized (hmap) {
                hmap.clear();
            }
        }
    }

    public int size() {
        int size = 0;
        for (LHash<V> hmap : maps) {
            size += hmap.size();
        }
        return size;
    }

}
