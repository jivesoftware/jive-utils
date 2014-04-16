package com.jivesoftware.os.jive.utils.keyed.store;

import com.jivesoftware.os.jive.utils.chunk.store.ChunkStore;
import com.jivesoftware.os.jive.utils.io.Filer;
import com.jivesoftware.os.jive.utils.map.store.FileBackMapStore;

import java.io.IOException;

/**
 * @author jonathan
 */
public class FileBackedKeyedStore implements KeyedFilerStore {

    private final FileBackMapStore<byte[], byte[]> mapStore;
    private final ChunkStore chunkStore;
    private final long newFilerInitialCapacity;

    public FileBackedKeyedStore(String mapDirectory, int mapKeySize, long initialMapKeyCapacity,
            ChunkStore chunkStore, long newFilerInitialCapacity) throws Exception
    {
        this.mapStore = initializeMapStore(mapDirectory, mapKeySize, initialMapKeyCapacity);
        this.chunkStore = chunkStore;
        this.newFilerInitialCapacity = newFilerInitialCapacity;
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

    @Override
    public Filer get(byte[] key) throws Exception {
        return get(key, true);
    }

    @Override
    public Filer get(byte[] key, boolean autoCreate) throws Exception {
        AutoResizingChunkFiler filer = new AutoResizingChunkFiler(mapStore, key, chunkStore);
        if (!autoCreate && !filer.exists()) {
            return null;
        }
        filer.init(newFilerInitialCapacity);
        return filer;
    }

    @Override
    public long sizeInBytes() throws IOException {
        return mapStore.sizeInBytes() + chunkStore.sizeInBytes();
    }

    @Override
    public void close() {
        // TODO
    }
}
