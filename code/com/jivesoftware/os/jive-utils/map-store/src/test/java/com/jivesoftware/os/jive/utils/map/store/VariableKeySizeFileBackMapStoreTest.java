package com.jivesoftware.os.jive.utils.map.store;

import com.google.common.base.Charsets;
import com.jivesoftware.os.jive.utils.io.FilerIO;
import com.jivesoftware.os.jive.utils.map.store.api.KeyValueStoreException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Files;

/**
 *
 */
public class VariableKeySizeFileBackMapStoreTest {

    private String pathToPartitions;

    @BeforeMethod
    public void setUp() throws Exception {
        pathToPartitions = Files.createTempDirectory(getClass().getSimpleName()).toFile().getAbsolutePath();
    }

    private VariableKeySizeFileBackMapStore<String, Long> createMapStore(String pathToPartitions, int[] keySizeThresholds) {
        return new VariableKeySizeFileBackMapStore<String, Long>(pathToPartitions, keySizeThresholds, 8, 10, 1, null) {

            @Override
            protected int keyLength(String key) {
                return key.getBytes(Charsets.US_ASCII).length;
            }

            @Override
            public String keyPartition(String key) {
                return "0";
            }

            @Override
            public byte[] keyBytes(String key) {
                return key.getBytes(Charsets.US_ASCII);
            }

            @Override
            public byte[] valueBytes(Long value) {
                return FilerIO.longBytes(value);
            }

            @Override
            public Long bytesValue(String key, byte[] bytes, int offset) {
                return FilerIO.bytesLong(bytes, offset);
            }
        };
    }

    @Test
    public void testAddGet() throws KeyValueStoreException {
        int[] keySizeThresholds = new int[]{4, 16, 64, 256, 1024};
        VariableKeySizeFileBackMapStore<String, Long> mapStore = createMapStore(pathToPartitions, keySizeThresholds);

        for (int i = 0; i < keySizeThresholds.length; i++) {
            String key = keyOfLength(keySizeThresholds[i]);
            long expected = (long) i;
            mapStore.add(key, expected);
            Assert.assertEquals(mapStore.get(key).longValue(), expected);
        }
    }

    @Test(expectedExceptions = {IndexOutOfBoundsException.class})
    public void testKeyTooBig() throws KeyValueStoreException {
        int[] keySizeThresholds = new int[]{1, 2, 4};
        VariableKeySizeFileBackMapStore<String, Long> mapStore = createMapStore(pathToPartitions, keySizeThresholds);

        int maxLength = keySizeThresholds[keySizeThresholds.length - 1];
        mapStore.add(keyOfLength(maxLength + 1), 0l);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testBadThresholds() throws KeyValueStoreException {
        int[] keySizeThresholds = new int[]{0, 0};
        createMapStore(pathToPartitions, keySizeThresholds);
    }

    private String keyOfLength(int length) {
        StringBuilder buf = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            buf.append('a');
        }
        return buf.toString();
    }
}