package com.jivesoftware.os.jive.utils.map.store.api;

public interface KeyValueStore<K, V> extends Iterable<KeyValueStore.Entry<K, V>> {

    String keyPartition(K key);

    Iterable<String> keyPartitions();

    byte[] keyBytes(K key);

    byte[] valueBytes(V value);

    K bytesKey(byte[] bytes, int offset);

    V bytesValue(K key, byte[] bytes, int offset);

    void add(K key, V value) throws KeyValueStoreException;

    void remove(K key) throws KeyValueStoreException;

    V get(K key) throws KeyValueStoreException;

    long estimatedMaxNumberOfKeys();

    interface Entry<K, V> {
        K getKey();

        V getValue();
    }
}
