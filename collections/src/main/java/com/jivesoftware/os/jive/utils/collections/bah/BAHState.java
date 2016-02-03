package com.jivesoftware.os.jive.utils.collections.bah;

/**
 *
 * @author jonathan.colt
 */
public interface BAHState<V> {

    BAHState<V> allocate(long capacity);

    byte[] skipped();

    long first();

    long size();

    void update(long i, byte[] key, V value);

    void link(long i, byte[] key, V value);

    void clear(long i);

    void remove(long i, byte[] key, V value);

    long next(long i);

    long capacity();

    byte[] key(long i);

    V value(long i);

}
