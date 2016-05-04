package com.jivesoftware.os.jive.utils.collections.oh;

import com.jivesoftware.os.jive.utils.collections.KeyValueStream;

/**
 *
 * @author jonathan.colt
 */
public interface OH<K, V> {

    void clear();

    V get(K key);

    V get(long hashCode, K key);

    void put(K key, V value);

    @SuppressWarnings(value = "unchecked")
    void put(long hashCode, K key, V value);

    void remove(K key);

    @SuppressWarnings(value = "unchecked")
    void remove(long hashCode, K key);

    long size();

    boolean stream(KeyValueStream<K, V> stream) throws Exception;

}
