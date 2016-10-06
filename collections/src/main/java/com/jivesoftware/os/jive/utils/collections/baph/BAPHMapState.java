package com.jivesoftware.os.jive.utils.collections.baph;

import java.util.Arrays;

/**
 *
 * @author jonathan.colt
 */
public class BAPHMapState<V> implements BAPHState<V> {

    public static final long NIL_POINTER = -2L;
    public static final byte[] NIL = new byte[0];

    private final long capacity;
    private final boolean hasValues;
    private final long nilKeyPointer;
    private final byte[] nilKey;
    private final BAPReader reader;

    private final long[] keyPointers;
    private final Object[] values;
    private int count;

    public BAPHMapState(long capacity, boolean hasValues, long nilKeyPointer, byte[] nilKey, BAPReader reader) {
        this.count = 0;
        this.capacity = capacity;
        this.hasValues = hasValues;
        this.nilKeyPointer = nilKeyPointer;
        this.nilKey = nilKey;
        this.reader = reader;

        this.keyPointers = new long[(int) capacity];
        Arrays.fill(this.keyPointers, -1L);
        this.values = (hasValues) ? new Object[(int) capacity] : null;
    }

    @Override
    public BAPHState<V> allocate(long capacity) {
        return new BAPHMapState<>(capacity, hasValues, nilKeyPointer, nilKey, reader);
    }

    @Override
    public long skippedPointer() {
        return nilKeyPointer;
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
    public void update(long i, long keyPointer, V value) {
        keyPointers[(int) i] = keyPointer;
        if (hasValues) {
            values[(int) i] = value;
        }
    }

    @Override
    public void link(long i, long keyPointer, V value) {
        keyPointers[(int) i] = keyPointer;
        if (hasValues) {
            values[(int) i] = value;
        }
        count++;
    }

    @Override
    public void clear(long i) {
        keyPointers[(int) i] = -1;
        if (hasValues) {
            values[(int) i] = null;
        }
    }

    @Override
    public void remove(long i, long keyPointer, V value) {
        keyPointers[(int) i] = keyPointer;
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

    public long[] getKeyPointers() {
        return keyPointers;
    }

    @Override
    public long keyPointer(long i) {
        return keyPointers[(int) i];
    }

    @Override
    public byte[] key(long i) {
        if (keyPointers[(int) i] == nilKeyPointer) {
            return nilKey;
        }
        if (keyPointers[(int) i] == -1) {
            return null;
        }
        return reader.byteArray(keyPointers[(int) i]);
    }

    @Override
    public V value(long i) {
        return (V) (hasValues ? values[(int) i] : reader.byteArray(keyPointers[(int) i]));
    }

}
