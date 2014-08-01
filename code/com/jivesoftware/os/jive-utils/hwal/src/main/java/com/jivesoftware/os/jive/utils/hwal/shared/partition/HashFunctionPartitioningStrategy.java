package com.jivesoftware.os.jive.utils.hwal.shared.partition;

import com.google.common.hash.HashFunction;

/**
 *
 * @author jonathan
 */
public class HashFunctionPartitioningStrategy implements WALPartitioningStrategy {

    private final HashFunction hashFunction;

    public HashFunctionPartitioningStrategy(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
    }

    @Override
    public int partition(byte[] key, int numberOfPartitions) {
        return hashFunction.hashBytes(key).asInt() % numberOfPartitions;
    }

}
