package com.jivesoftware.os.jive.utils.collections.oh;

/**
 *
 * @author jonathan.colt
 */
public interface OHState<K, V> {

    OHState<K,V> allocate(long capacity);

    K skipped();

    long first();

    long size();

    void update(long i, K key, V value);

    void link(long i, K key, V value);

    void clear(long i);

    void remove(long i, K key, V value);

    long next(long i);

    long capacity();

    K key(long i);

    V value(long i);

}
