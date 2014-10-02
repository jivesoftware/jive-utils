package com.jivesoftware.os.jive.utils.keyed.store;


/**
 *
 */
public interface KeyedFilerStore {

    public SwappableFiler get(byte[] key) throws Exception;

    public SwappableFiler get(byte[] key, boolean autoCreate) throws Exception;

    public long sizeInBytes() throws Exception;

    public void close();
}
