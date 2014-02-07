package com.jivesoftware.os.jive.utils.io;

import java.nio.ByteBuffer;

/**
 *
 * @author jonathan
 */
public interface ByteBufferFactory {

    /**
     *
     * @param _size
     * @return
     */
    ByteBuffer allocate(long _size);

}
