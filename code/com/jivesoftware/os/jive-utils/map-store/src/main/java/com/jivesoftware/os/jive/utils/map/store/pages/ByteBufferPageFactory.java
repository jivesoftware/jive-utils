package com.jivesoftware.os.jive.utils.map.store.pages;

import java.nio.ByteBuffer;

/**
 *
 * @author jonathan.colt
 */
public class ByteBufferPageFactory implements PageFactory {

    @Override
    public Page allocate(long _size) {
        return new ByteBufferPage(ByteBuffer.allocate((int) _size));
    }
}
