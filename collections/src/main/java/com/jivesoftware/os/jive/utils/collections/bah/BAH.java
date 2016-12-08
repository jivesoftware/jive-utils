package com.jivesoftware.os.jive.utils.collections.bah;

import com.jivesoftware.os.jive.utils.collections.KeyValueStream;
import java.util.concurrent.Semaphore;

/**
 * @author jonathan.colt
 */
public interface BAH<V> {

    void clear();

    V get(byte[] key, int keyOffset, int keyLength);

    V get(long hashCode, byte[] key, int keyOffset, int keyLength);

    void put(byte[] key, V value);

    @SuppressWarnings(value = "unchecked")
    void put(long hashCode, byte[] key, V value);

    void remove(byte[] key, int keyOffset, int keyLength);

    @SuppressWarnings(value = "unchecked")
    void remove(long hashCode, byte[] key, int keyOffset, int keyLength);

    long size();

    boolean stream(Semaphore semaphore, KeyValueStream<byte[], V> stream) throws Exception;

}
