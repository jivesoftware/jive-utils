package com.jivesoftware.os.jive.utils.collections.lh;

import java.util.Arrays;

/**
 *
 * @author jonathan.colt
 */
public class LHMapState<V> {

    public static final byte[] NIL = new byte[0];

    private final LHMapStateManager<V> manager;
    private final long capacity;
    private final long nilKey;
    private final long skipKey;
    private final long[] keys;
    private final Object[] values;
    private int count;

    public LHMapState(LHMapStateManager<V> manager, long capacity, long nilKey, long skipKey) {
        this.manager = manager;
        this.count = 0;
        this.capacity = capacity;
        this.nilKey = nilKey;
        this.skipKey = skipKey;

        this.keys = new long[(int) capacity];
        Arrays.fill(keys, nilKey);
        this.values = new Object[(int) capacity];
    }

    public LHMapState<V> allocate(long capacity) {
        LHMapState<V> state = new LHMapState<>(manager, capacity, nilKey, skipKey);
        manager.allocated(state);
        return state;
    }

    public void deallocate(LHMapState<V> state) {
        manager.deallocated(state);
    }

    public long skipped() {
        return skipKey;
    }

    public long nil() {
        return nilKey;
    }

    public long first() {
        return 0;
    }

    public long size() {
        return count;
    }

    public void update(long i, long key, V value) {
        keys[(int) i] = key;
        values[(int) i] = value;
    }

    public void link(long i, long key, V value) {
        keys[(int) i] = key;
        values[(int) i] = value;
        count++;
    }

    public void clear(long i) {
        keys[(int) i] = nilKey;
        values[(int) i] = null;
    }

    public void remove(long i, long key, V value) {
        keys[(int) i] = key;
        values[(int) i] = value;
        count--;
    }

    public long next(long i) {
        return (i >= capacity - 1) ? -1 : i + 1;
    }

    public long capacity() {
        return capacity;
    }

    public long key(long i) {
        return keys[(int) i];
    }

    public V value(long i) {
        return (V) values[(int) i];
    }

}
