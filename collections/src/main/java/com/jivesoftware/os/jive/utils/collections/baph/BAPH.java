package com.jivesoftware.os.jive.utils.collections.baph;

import com.jivesoftware.os.jive.utils.collections.KeyValueStream;

/**
 *
 * @author jonathan.colt
 */
public interface BAPH<V> {

    void clear();

    V get(byte[] key, int keyOffset, int keyLength) throws Exception;

    V get(long hashCode, byte[] key, int keyOffset, int keyLength) throws Exception;

    void put(long keyPointer, byte[] key, V value) throws Exception;

    @SuppressWarnings(value = "unchecked")
    void put(long hashCode, long keyPointer, byte[] key, V value) throws Exception;

    void remove(byte[] key, int keyOffset, int keyLength) throws Exception;

    @SuppressWarnings(value = "unchecked")
    void remove(long hashCode, byte[] key, int keyOffset, int keyLength) throws Exception;

    long size();

    boolean stream(KeyValueStream<byte[], V> stream) throws Exception;

}
