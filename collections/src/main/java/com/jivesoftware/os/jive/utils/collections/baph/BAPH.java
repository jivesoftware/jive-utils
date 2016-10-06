package com.jivesoftware.os.jive.utils.collections.baph;

import com.jivesoftware.os.jive.utils.collections.KeyValueStream;

/**
 *
 * @author jonathan.colt
 */
public interface BAPH<V> {

    void clear();

    V get(byte[] key, int keyOffset, int keyLength);

    V get(long hashCode, byte[] key, int keyOffset, int keyLength);

    void put(long keyPointer, byte[] key, V value);

    @SuppressWarnings(value = "unchecked")
    void put(long hashCode, long keyPointer, byte[] key, V value);

    void remove(byte[] key, int keyOffset, int keyLength);

    @SuppressWarnings(value = "unchecked")
    void remove(long hashCode, byte[] key, int keyOffset, int keyLength);

    long size();

    boolean stream(KeyValueStream<byte[], V> stream) throws Exception;

}
