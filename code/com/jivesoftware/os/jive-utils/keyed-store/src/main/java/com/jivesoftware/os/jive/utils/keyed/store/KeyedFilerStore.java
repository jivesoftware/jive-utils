package com.jivesoftware.os.jive.utils.keyed.store;


import java.io.IOException;

/**
 *
 */
public interface KeyedFilerStore {

    public SwappableFiler get(byte[] key) throws Exception;

    public SwappableFiler get(byte[] key, boolean autoCreate) throws Exception;

    public long sizeInBytes() throws IOException;

    public void close();
}
