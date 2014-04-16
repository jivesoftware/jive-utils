package com.jivesoftware.os.jive.utils.keyed.store;

import com.jivesoftware.os.jive.utils.io.Filer;

import java.io.IOException;

/**
 *
 */
public interface KeyedFilerStore {

    public Filer get(byte[] key) throws Exception;

    public Filer get(byte[] key, boolean autoCreate) throws Exception;

    public long sizeInBytes() throws IOException;

    public void close();
}
