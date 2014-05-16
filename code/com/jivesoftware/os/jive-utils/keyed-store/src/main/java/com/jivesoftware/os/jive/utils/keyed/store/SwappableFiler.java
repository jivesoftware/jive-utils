package com.jivesoftware.os.jive.utils.keyed.store;

import com.jivesoftware.os.jive.utils.io.Filer;

/**
 *
 */
public interface SwappableFiler extends Filer {

    SwappingFiler swap(long initialChunkSize) throws Exception;
}
