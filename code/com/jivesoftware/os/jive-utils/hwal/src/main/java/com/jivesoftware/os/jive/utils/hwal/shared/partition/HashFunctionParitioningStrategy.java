package com.jivesoftware.os.jive.utils.hwal.shared.partition;

import com.google.common.hash.HashFunction;
import com.jivesoftware.os.jive.utils.hwal.shared.api.WALKey;

/**
 *
 * @author jonathan
 */
public class HashFunctionParitioningStrategy implements WALParitioningStrategy {

    private final HashFunction hashFunction;

    public HashFunctionParitioningStrategy(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
    }

    @Override
    public int parition(WALKey key, int numberOfPartitions) {
        return hashFunction.hashBytes(key.getBytes()).asInt() & numberOfPartitions;
    }

}
