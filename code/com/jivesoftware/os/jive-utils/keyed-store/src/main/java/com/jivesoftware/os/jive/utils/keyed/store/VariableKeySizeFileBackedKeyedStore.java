package com.jivesoftware.os.jive.utils.keyed.store;

import com.google.common.base.Preconditions;
import com.jivesoftware.os.jive.utils.chunk.store.MultiChunkStore;
import java.io.File;

public class VariableKeySizeFileBackedKeyedStore implements KeyedFilerStore {

    private final int[] keySizeThresholds;
    private final FileBackedKeyedStore[] keyedStores;

    public VariableKeySizeFileBackedKeyedStore(File baseMapDirectory, File baseSwapDirectory, MultiChunkStore chunkStore, int[] keySizeThresholds,
            long initialMapKeyCapacity, long newFilerInitialCapacity, int numPartitions) throws Exception
    {
        this.keySizeThresholds = keySizeThresholds;
        this.keyedStores = new FileBackedKeyedStore[keySizeThresholds.length];
        for (int i = 0; i < keySizeThresholds.length; i++) {
            Preconditions.checkArgument(i == 0 || keySizeThresholds[i] > keySizeThresholds[i - 1], "Thresholds must be monotonically increasing");

            final int keySize = keySizeThresholds[i];
            String mapDirectory = new File(baseMapDirectory, String.valueOf(keySize)).getAbsolutePath();
            String swapDirectory = new File(baseSwapDirectory, String.valueOf(keySize)).getAbsolutePath();
            keyedStores[i] = new FileBackedKeyedStore(mapDirectory, swapDirectory, keySize, initialMapKeyCapacity, chunkStore, newFilerInitialCapacity,
                numPartitions);
        }
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