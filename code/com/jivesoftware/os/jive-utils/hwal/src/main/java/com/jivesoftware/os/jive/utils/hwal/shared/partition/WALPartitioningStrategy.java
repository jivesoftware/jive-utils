package com.jivesoftware.os.jive.utils.hwal.shared.partition;

/**
 *
 * @author jonathan.colt
 */
public interface WALPartitioningStrategy {

    int partition(byte[] key, int numberOfPartitions);
}
