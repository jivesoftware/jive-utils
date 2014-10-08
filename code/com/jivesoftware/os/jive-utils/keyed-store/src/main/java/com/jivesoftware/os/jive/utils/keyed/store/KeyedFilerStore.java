package com.jivesoftware.os.jive.utils.keyed.store;


/**
 *
 */
public interface KeyedFilerStore {

    SwappableFiler get(byte[] keyBytes, long newFilerInitialCapacity) throws Exception;

    long sizeInBytes() throws Exception;

    void close();
}
