package com.jivesoftware.os.jive.utils.map.store.pages;

import java.nio.ByteBuffer;

/**
 *
 * @author jonathan.colt
 */
public class DirectByteBufferPageFactory implements PageFactory {

    @Override
    public Page allocate(long _size) {
        return new ByteBufferPage(ByteBuffer.allocateDirect((int) _size));
    }
}
