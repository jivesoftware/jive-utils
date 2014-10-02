package com.jivesoftware.os.jive.utils.map.store.api;

public interface PartitionedKeyValueStore<K, V> extends KeyValueStore<K, V> {

    String keyPartition(K key);

    Iterable<String> keyPartitions();
}
