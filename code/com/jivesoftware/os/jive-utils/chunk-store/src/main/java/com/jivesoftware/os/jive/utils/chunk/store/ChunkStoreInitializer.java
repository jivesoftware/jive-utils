package com.jivesoftware.os.jive.utils.chunk.store;

import com.jivesoftware.os.jive.utils.io.ByteBufferBackedFiler;
import com.jivesoftware.os.jive.utils.io.FileBackedMemMappedByteBufferFactory;
import com.jivesoftware.os.jive.utils.io.SubsetableFiler;

import java.io.File;
import java.nio.ByteBuffer;

/**
 *
 */
public class ChunkStoreInitializer {

    private static final long referenceNumber = 1;

    public ChunkStore initialize(String chunkFile, long chunkStoreCapacityInBytes) throws Exception {
        File chunkStoreFile = new File(chunkFile);
        FileBackedMemMappedByteBufferFactory bufferFactory = new FileBackedMemMappedByteBufferFactory(chunkStoreFile);

        ChunkStore chunkStore;
        if (chunkStoreFile.exists() && chunkStoreFile.length() > 0) {
            ByteBuffer byteBuffer = bufferFactory.allocate(chunkStoreCapacityInBytes);
            ByteBufferBackedFiler byteBufferBackedFiler = new ByteBufferBackedFiler(chunkStoreFile, byteBuffer);
            chunkStore = new ChunkStore(new SubsetableFiler(byteBufferBackedFiler, 0, chunkStoreCapacityInBytes, 0));
            chunkStore.open();
        } else {
            ByteBuffer byteBuffer = bufferFactory.allocate(chunkStoreCapacityInBytes);
            ByteBufferBackedFiler byteBufferBackedFiler = new ByteBufferBackedFiler(chunkStoreFile, byteBuffer);
            chunkStore = new ChunkStore();
            chunkStore.setup(referenceNumber);
            chunkStore.open(new SubsetableFiler(byteBufferBackedFiler, 0, chunkStoreCapacityInBytes, 0));
        }
        return chunkStore;
    }
}
