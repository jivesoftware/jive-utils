package com.jivesoftware.os.jive.utils.map.store.pages;

import java.nio.ByteBuffer;

/**
 *
 * @author jonathan.colt
 */
public class DirectByteBufferChunkFactory implements ChunkFactory {

    @Override
    public Chunk allocate(long _size) {
        return new ByteBufferChunk(ByteBuffer.allocateDirect((int) _size));
    }
}
