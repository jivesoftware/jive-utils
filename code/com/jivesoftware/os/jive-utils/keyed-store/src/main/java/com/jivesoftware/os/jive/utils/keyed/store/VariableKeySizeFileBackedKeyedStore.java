package com.jivesoftware.os.jive.utils.keyed.store;

import com.google.common.base.Preconditions;
import com.jivesoftware.os.jive.utils.chunk.store.MultiChunkStore;
import java.io.File;

public class VariableKeySizeFileBackedKeyedStore implements KeyedFilerStore {

    private final int[] keySizeThresholds;
    private final FileBackedKeyedStore[] keyedStores;

    public VariableKeySizeFileBackedKeyedStore(String[] baseMapDirectories,
        String[] baseSwapDirectories,
        MultiChunkStore chunkStore,
        int[] keySizeThresholds,
        long initialMapKeyCapacity,
        long newFilerInitialCapacity,
        int numPartitions)
        throws Exception {

        this.keySizeThresholds = keySizeThresholds;
        this.keyedStores = new FileBackedKeyedStore[keySizeThresholds.length];
        for (int keySizeIndex = 0; keySizeIndex < keySizeThresholds.length; keySizeIndex++) {
            Preconditions.checkArgument(keySizeIndex == 0 || keySizeThresholds[keySizeIndex] > keySizeThresholds[keySizeIndex - 1],
                "Thresholds must be monotonically increasing");

            final int keySize = keySizeThresholds[keySizeIndex];
            String[] mapDirectories = buildMapDirectories(baseMapDirectories, keySize);
            String[] swapDirectories = buildMapDirectories(baseSwapDirectories, keySize);
            keyedStores[keySizeIndex] = new FileBackedKeyedStore(mapDirectories, swapDirectories, keySize, initialMapKeyCapacity, chunkStore,
                newFilerInitialCapacity, numPartitions);
        }
    }

    private String[] buildMapDirectories(String[] baseMapDirectories, int keySize) {
        String[] mapDirectories = new String[baseMapDirectories.length];
        for (int basePathIndex = 0; basePathIndex < baseMapDirectories.length; basePathIndex++) {
            mapDirectories[basePathIndex] = new File(baseMapDirectories[basePathIndex], String.valueOf(keySize)).getAbsolutePath();
        }
        return mapDirectories;
    }

    private int keySize(byte[] key) {
        for (int keySize : keySizeThresholds) {
            if (keySize >= key.length) {
                return keySize;
            }
        }
        throw new IndexOutOfBoundsException("Key is too long");
    }

    private FileBackedKeyedStore getKeyedStore(byte[] key) {
        for (int i = 0; i < keySizeThresholds.length; i++) {
            if (keySizeThresholds[i] >= key.length) {
                return keyedStores[i];
            }
        }
        throw new IndexOutOfBoundsException("Key is too long");
    }

    private byte[] pad(byte[] key) {
        int keySize = keySize(key);
        byte[] padded = new byte[keySize];
        System.arraycopy(key, 0, padded, 0, key.length);
        return padded;
    }

    @Override
    public SwappableFiler get(byte[] key) throws Exception {
        return getKeyedStore(key).get(pad(key));
    }

    @Override
    public SwappableFiler get(byte[] key, boolean autoCreate) throws Exception {
        return getKeyedStore(key).get(pad(key), autoCreate);
    }

    @Override
    public long sizeInBytes() throws Exception {
        long sizeInBytes = 0;
        for (FileBackedKeyedStore keyedStore : keyedStores) {
            sizeInBytes += keyedStore.mapStoreSizeInBytes();
        }
        return sizeInBytes;
    }

    @Override
    public void close() {
        for (FileBackedKeyedStore keyedStore : keyedStores) {
            keyedStore.close();
        }
    }
}