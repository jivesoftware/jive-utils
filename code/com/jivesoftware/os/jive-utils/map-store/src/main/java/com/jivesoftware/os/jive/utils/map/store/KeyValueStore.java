package com.jivesoftware.os.jive.utils.map.store;

public interface KeyValueStore<K, V> {

    String keyPartition(K key);

    byte[] keyBytes(K key);

    byte[] valueBytes(V value);

    V bytesValue(K key, byte[] bytes, int offset);

    void add(K key, V value) throws KeyValueStoreException;

    void remove(K key) throws KeyValueStoreException;

    V get(K key) throws KeyValueStoreException;

    long estimatedMaxNumberOfKeys();
}
