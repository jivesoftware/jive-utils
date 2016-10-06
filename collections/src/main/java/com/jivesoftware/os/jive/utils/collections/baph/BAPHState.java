package com.jivesoftware.os.jive.utils.collections.baph;

/**
 *
 * @author jonathan.colt
 */
public interface BAPHState<V> {

    BAPHState<V> allocate(long capacity);

    long skippedPointer();

    byte[] skipped();

    long first();

    long size();

    void update(long i, long keyPointer, V value);

    void link(long i, long keyPointer, V value);

    void clear(long i);

    void remove(long i, long keyPointer, V value);

    long next(long i);

    long capacity();

    long keyPointer(long i);

    byte[] key(long i) throws Exception;

    V value(long i) throws Exception;

}
