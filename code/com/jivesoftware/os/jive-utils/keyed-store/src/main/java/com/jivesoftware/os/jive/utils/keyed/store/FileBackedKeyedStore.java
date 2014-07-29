package com.jivesoftware.os.jive.utils.keyed.store;

import com.jivesoftware.os.jive.utils.chunk.store.ChunkStore;
import com.jivesoftware.os.jive.utils.map.store.FileBackMapStore;

import java.io.IOException;
import java.util.Collections;

/**
 * @author jonathan
 */
public class FileBackedKeyedStore implements KeyedFilerStore {

    private final FileBackMapStore<IBA, IBA> mapStore;
    private final FileBackMapStore<IBA, IBA> swapStore;
    private final ChunkStore chunkStore;
    private final long newFilerInitialCapacity;

    public FileBackedKeyedStore(String mapDirectory, String swapDirectory, int mapKeySize, long initialMapKeyCapacity,
            ChunkStore chunkStore, long newFilerInitialCapacity) throws Exception
    {
        this.mapStore = initializeMapStore(mapDirectory, mapKeySize, initialMapKeyCapacity);
        this.swapStore = initializeMapStore(swapDirectory, mapKeySize, initialMapKeyCapacity);
        this.chunkStore = chunkStore;
        this.newFilerInitialCapacity = newFilerInitialCapacity;
    }

    private FileBackMapStore<IBA, IBA> initializeMapStore(String mapDirectory, int mapKeySize, long initialMapKeyCapacity) throws Exception {
        return new FileBackMapStore<IBA, IBA>(mapDirectory, mapKeySize, 8, (int) initialMapKeyCapacity, 100, null) {
            @Override
            public String keyPartition(IBA key) {
                return "_";
            }

            @Override
            public Iterable<String> keyPartitions() {
                return Collections.singletonList("_");
            }

            @Override
            public byte[] keyBytes(IBA key) {
                return key.getBytes();
            }

            @Override
            public byte[] valueBytes(IBA value) {
                return value.getBytes();
            }

            @Override
            public IBA bytesKey(byte[] keyBytes, int offset) {
                return new IBA(keyBytes);
            }

            @Override
            public IBA bytesValue(IBA key, byte[] value, int valueOffset) {
                return new IBA(value);
            }
        };
    }

    @Override
    public SwappableFiler get(byte[] key) throws Exception {
        return get(key, true);
    }

    @Override
    public SwappableFiler get(byte[] keyBytes, boolean autoCreate) throws Exception {
        IBA key = new IBA(keyBytes);
        AutoResizingChunkFiler filer = new AutoResizingChunkFiler(mapStore, key, chunkStore);
        if (!autoCreate && !filer.exists()) {
            return null;
        }
        filer.init(newFilerInitialCapacity);
        return new AutoResizingChunkSwappableFiler(filer, chunkStore, key, mapStore, swapStore);
    }

    @Override
    public long sizeInBytes() throws IOException {
        return mapStore.sizeInBytes() + chunkStore.sizeInBytes();
    }

    public long mapStoreSizeInBytes() throws IOException {
        return mapStore.sizeInBytes();
    }

    @Override
    public void close() {
        // TODO
    }
}
