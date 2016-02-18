package com.jivesoftware.os.jive.utils.collections.bah;

/**
 *
 * @author jonathan.colt
 */
public class BAHMapState<V> implements BAHState<V> {

    public static final byte[] NIL = new byte[0];

    private final long capacity;
    private final boolean hasValues;
    private final byte[] nilKey;
    private final byte[][] keys;
    private final Object[] values;
    private int count;

    public BAHMapState(long capacity, boolean hasValues, byte[] nilKey) {
        this.count = 0;
        this.capacity = capacity;
        this.hasValues = hasValues;
        this.nilKey = nilKey;

        this.keys = new byte[(int) capacity][];
        this.values = (hasValues) ? new Object[(int) capacity] : null;
    }

    @Override
    public BAHState<V> allocate(long capacity) {
        return new BAHMapState<>(capacity, hasValues, nilKey);
    }

    @Override
    public byte[] skipped() {
        return nilKey;
    }

    @Override
    public long first() {
        return 0;
    }

    @Override
    public long size() {
        return count;
    }

    @Override
    public void update(long i, byte[] key, V value) {
        keys[(int) i] = key;
        if (hasValues) {
            values[(int) i] = value;
        }
    }

    @Override
    public void link(long i, byte[] key, V value) {
        keys[(int) i] = key;
        if (hasValues) {
            values[(int) i] = value;
        }
        count++;
    }

    @Override
    public void clear(long i) {
        keys[(int) i] = null;
        if (hasValues) {
            values[(int) i] = null;
        }
    }

    @Override
    public void remove(long i, byte[] key, V value) {
        keys[(int) i] = key;
        if (hasValues) {
            values[(int) i] = value;
        }
        count--;
    }

    @Override
    public long next(long i) {
        return (i >= capacity - 1) ? -1 : i + 1;
    }

    @Override
    public long capacity() {
        return capacity;
    }

    @Override
    public byte[] key(long i) {
        return (byte[]) keys[(int) i];
    }

    @Override
    public V value(long i) {
        return (V) (hasValues ? values[(int) i] : keys[(int) i]);
    }

}
