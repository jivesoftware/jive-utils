package com.jivesoftware.os.jive.utils.chunk.store;

import com.jivesoftware.os.jive.utils.io.AutoResizingByteBufferBackedFiler;
import com.jivesoftware.os.jive.utils.io.ByteBufferBackedFiler;
import com.jivesoftware.os.jive.utils.io.FileBackedMemMappedByteBufferFactory;
import com.jivesoftware.os.jive.utils.io.Filer;
import com.jivesoftware.os.jive.utils.io.SubsetableFiler;
import java.io.File;

/**
 *
 */
public class ChunkStoreInitializer {

    private static final long referenceNumber = 1;

    public ChunkStore initialize(String chunkFile, long chunkStoreCapacityInBytes, boolean autoResize) throws Exception {
        File chunkStoreFile = new File(chunkFile);
        FileBackedMemMappedByteBufferFactory bufferFactory = new FileBackedMemMappedByteBufferFactory(chunkStoreFile);

        ChunkStore chunkStore;
        if (chunkStoreFile.exists() && chunkStoreFile.length() > 0) {
            Filer filer;
            if (autoResize) {
                filer = new AutoResizingByteBufferBackedFiler(chunkStoreFile, chunkStoreFile.length(), bufferFactory);
                chunkStore = new ChunkStore(new SubsetableFiler(filer, 0, Long.MAX_VALUE, 0));
            } else {
                filer = new ByteBufferBackedFiler(chunkStoreFile, bufferFactory.allocate(chunkStoreCapacityInBytes));
                chunkStore = new ChunkStore(new SubsetableFiler(filer, 0, chunkStoreCapacityInBytes, 0));
            }
            chunkStore.open();
        } else {
            chunkStore = new ChunkStore();
            chunkStore.setup(referenceNumber);

            Filer filer;
            if (autoResize) {
                filer = new AutoResizingByteBufferBackedFiler(chunkStoreFile, chunkStoreCapacityInBytes, bufferFactory);
                chunkStore.open(new SubsetableFiler(filer, 0, Long.MAX_VALUE, 0));
            } else {
                filer = new ByteBufferBackedFiler(chunkStoreFile, bufferFactory.allocate(chunkStoreCapacityInBytes));
                chunkStore.open(new SubsetableFiler(filer, 0, chunkStoreCapacityInBytes, 0));
            }
        }
        return chunkStore;
    }
}
