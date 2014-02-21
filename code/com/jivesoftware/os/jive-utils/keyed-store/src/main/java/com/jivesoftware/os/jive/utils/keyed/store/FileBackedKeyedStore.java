package com.jivesoftware.os.jive.utils.keyed.store;

import com.jivesoftware.os.jive.utils.chunk.store.ChunkStore;
import com.jivesoftware.os.jive.utils.io.ByteBufferBackedFiler;
import com.jivesoftware.os.jive.utils.io.FileBackedMemMappedByteBufferFactory;
import com.jivesoftware.os.jive.utils.io.Filer;
import com.jivesoftware.os.jive.utils.io.SubsetableFiler;
import com.jivesoftware.os.jive.utils.map.store.FileBackMapStore;
import java.io.File;
import java.nio.ByteBuffer;

/**
 *
 * @author jonathan
 */
public class FileBackedKeyedStore {

    private final String mapDirectory;
    private final int mapKeySize;
    private final long initialMapKeyCapacity;
    private final String chunkFile;
    private final long chunkStoreCapacityInBytes;
    private final long newFilerInitialCapacity;

    private final ChunkStore chunkStore;
    private final FileBackMapStore<byte[], byte[]> mapStore;

    public FileBackedKeyedStore(String mapDirectory, int mapKeySize, long initialMapKeyCapacity,
            String chunkFile, long chunkStoreCapacityInBytes, long newFilerInitialCapacity) throws Exception {
        this.mapDirectory = mapDirectory;
        this.mapKeySize = mapKeySize;
        this.initialMapKeyCapacity = initialMapKeyCapacity;
        this.chunkFile = chunkFile;
        this.chunkStoreCapacityInBytes = chunkStoreCapacityInBytes;
        this.newFilerInitialCapacity = newFilerInitialCapacity;
        mapStore = initializeMapStore(mapDirectory, mapKeySize, initialMapKeyCapacity);
        chunkStore = initializeChunkStore(chunkFile, chunkStoreCapacityInBytes);
    }

    private FileBackMapStore<byte[], byte[]> initializeMapStore(String mapDirectory, int mapKeySize, long initialMapKeyCapacity) throws Exception {
        return new FileBackMapStore<byte[], byte[]>(mapDirectory, mapKeySize, 8, (int) initialMapKeyCapacity, 100, null) {
            @Override
            public String keyPartition(byte[] bytes) {
                return "_";
            }

            @Override
            public byte[] keyBytes(byte[] bytes) {
                return bytes;
            }

            @Override
            public byte[] valueBytes(byte[] bytes) {
                return bytes;
            }

            @Override
            public byte[] bytesValue(byte[] key, byte[] value, int valueOffset) {
                return value;
            }
        };
    }

    private ChunkStore initializeChunkStore(String chunkFile, long chunkStoreCapacityInBytes) throws Exception {
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

    public Filer get(byte[] key) throws Exception {
        AutoResizingChunkFiler filer = new AutoResizingChunkFiler(mapStore, key, chunkStore);
        filer.init(newFilerInitialCapacity);
        return filer;
    }

    public void close() {
        // TODO
    }
}
