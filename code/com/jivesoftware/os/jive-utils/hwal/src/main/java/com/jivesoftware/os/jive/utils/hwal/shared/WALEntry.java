package com.jivesoftware.os.jive.utils.hwal.shared;

/**
 *
 * @author jonathan.colt
 * @param <P>
 * @param <V>
 */
public class WALEntry<P, V> {

    private final P partitionKey;
    private final V value;

    public WALEntry(P partitionKey, V value) {
        this.partitionKey = partitionKey;
        this.value = value;
    }

    public P getPartitionKey() {
        return partitionKey;
    }

    public V getValue() {
        return value;
    }
}
