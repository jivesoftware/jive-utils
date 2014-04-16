package com.jivesoftware.os.jive.utils.keyed.store;

import com.jivesoftware.os.jive.utils.chunk.store.ChunkStore;
import com.jivesoftware.os.jive.utils.io.ByteBufferBackedFiler;
import com.jivesoftware.os.jive.utils.io.FileBackedMemMappedByteBufferFactory;
import com.jivesoftware.os.jive.utils.io.SubsetableFiler;

import java.io.File;
import java.nio.ByteBuffer;

/**
 *
 */
public class ChunkStoreInitializer {

    public ChunkStore initialize(String chunkFile, long chunkStoreCapacityInBytes) throws Exception {
        ChunkStore newChunkStore = new ChunkStore();
        File chunkStoreFile = new File(chunkFile);
        FileBackedMemMappedByteBufferFactory bufferFactory = new FileBackedMemMappedByteBufferFactory(chunkStoreFile);
        ByteBuffer byteBuffer;
        if (chunkStoreFile.exists()) {
            byteBuffer = bufferFactory.open();
        } else {
            byteBuffer = bufferFactory.allocate(chunkStoreCapacityInBytes);
        }
        ByteBufferBackedFiler byteBufferBackedFiler = new ByteBufferBackedFiler(chunkStoreFile, byteBuffer);
        newChunkStore.open(new SubsetableFiler(byteBufferBackedFiler, 0, chunkStoreCapacityInBytes, 0));
        return newChunkStore;
    }
}
