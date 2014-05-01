package com.jivesoftware.os.jive.utils.keyed.store;

import com.jivesoftware.os.jive.utils.io.Filer;

/**
 *
 */
public interface SwappingFiler extends Filer {

    void commit() throws Exception;
}
