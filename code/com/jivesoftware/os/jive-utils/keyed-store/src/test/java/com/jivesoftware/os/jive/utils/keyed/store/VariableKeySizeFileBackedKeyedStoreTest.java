package com.jivesoftware.os.jive.utils.keyed.store;

import com.google.common.base.Charsets;
import com.jivesoftware.os.jive.utils.chunk.store.ChunkStore;
import com.jivesoftware.os.jive.utils.chunk.store.ChunkStoreInitializer;
import com.jivesoftware.os.jive.utils.chunk.store.MultiChunkStore;
import com.jivesoftware.os.jive.utils.io.Filer;
import com.jivesoftware.os.jive.utils.io.FilerIO;
import java.io.File;
import java.nio.file.Files;
import org.apache.commons.math.util.MathUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 */
public class VariableKeySizeFileBackedKeyedStoreTest {

    private File mapDirectory;
    private File swapDirectory;
    private File chunkDirectory;

    @BeforeMethod
    public void setUp() throws Exception {
        mapDirectory = Files.createTempDirectory(getClass().getSimpleName()).toFile();
        swapDirectory = Files.createTempDirectory(getClass().getSimpleName()).toFile();
        chunkDirectory = Files.createTempDirectory(getClass().getSimpleName()).toFile();
    }

    @Test
    public void testFilerAutoCreate() throws Exception {
        final int[] keySizeThresholds = new int[]{4, 16, 64, 256, 1024};
        File chunks = new File(chunkDirectory, "chunks");
        int chunkStoreCapacityInBytes = 30 * 1024 * 1024;
        ChunkStore chunkStore = new ChunkStoreInitializer().initialize(chunks.getAbsolutePath(), chunkStoreCapacityInBytes, false);
        MultiChunkStore multChunkStore = new MultiChunkStore(chunkStore);
        VariableKeySizeFileBackedKeyedStore keyedStore = new VariableKeySizeFileBackedKeyedStore(
                mapDirectory, swapDirectory, multChunkStore, keySizeThresholds, 100, 512);

        for (int keySize : keySizeThresholds) {
            Filer filer = keyedStore.get(keyOfLength(keySize), true);
            synchronized (filer.lock()) {
                filer.seek(0);
                FilerIO.writeInt(filer, keySize, "keySize");
            }
        }

        for (int keySize : keySizeThresholds) {
            Filer filer = keyedStore.get(keyOfLength(keySize), false);
            synchronized (filer.lock()) {
                filer.seek(0);
                int actual = FilerIO.readInt(filer, "keySize");
                Assert.assertEquals(actual, keySize);
            }
        }
    }

    @Test
    public void testFilerGrowsCapacity() throws Exception {
        final int[] keySizeThresholds = new int[]{4, 16};
        File chunks = new File(chunkDirectory, "chunks");
        int chunkStoreCapacityInBytes = 30 * 1024 * 1024;
        int newFilerInitialCapacity = 512;
        ChunkStore chunkStore = new ChunkStoreInitializer().initialize(chunks.getAbsolutePath(), chunkStoreCapacityInBytes, false);
        MultiChunkStore multChunkStore = new MultiChunkStore(chunkStore);
        VariableKeySizeFileBackedKeyedStore keyedStore = new VariableKeySizeFileBackedKeyedStore(
                mapDirectory, swapDirectory, multChunkStore, keySizeThresholds, 100, newFilerInitialCapacity);

        int numberOfIntsInInitialCapacity = newFilerInitialCapacity / 4;
        int numberOfIntsInActualCapacity = numberOfIntsInInitialCapacity * 2; // actual capacity is doubled
        int numberOfTimesToGrow = 3;
        int totalNumberOfInts = numberOfIntsInActualCapacity * MathUtils.pow(2, numberOfTimesToGrow - 1);

        for (int keySize : keySizeThresholds) {
            Filer filer = keyedStore.get(keyOfLength(keySize), true);
            synchronized (filer.lock()) {
                filer.seek(0);
                for (int i = 0; i < totalNumberOfInts; i++) {
                    FilerIO.writeInt(filer, i, String.valueOf(i));
                }
            }
        }

        for (int keySize : keySizeThresholds) {
            Filer filer = keyedStore.get(keyOfLength(keySize), true);
            synchronized (filer.lock()) {
                filer.seek(0);
                for (int i = 0; i < totalNumberOfInts; i++) {
                    int actual = FilerIO.readInt(filer, String.valueOf(i));
                    Assert.assertEquals(actual, i);
                }
            }
        }
    }

    private byte[] keyOfLength(int length) {
        StringBuilder buf = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            buf.append('a');
        }
        return buf.toString().getBytes(Charsets.US_ASCII);
    }
}
