package com.jivesoftware.os.jive.utils.map.store;

import com.jivesoftware.os.jive.utils.map.store.extractors.ExtractIndex;
import com.jivesoftware.os.jive.utils.map.store.extractors.ExtractKey;
import com.jivesoftware.os.jive.utils.map.store.extractors.ExtractPayload;
import com.jivesoftware.os.jive.utils.map.store.pages.ByteBufferPage;
import com.jivesoftware.os.jive.utils.map.store.pages.FileBackedMemMappedByteBufferPageFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.FileUtils;

/**
 * @author jonathan
 * @param <K>
 * @param <V>
 */
abstract public class KeyIndexer<K, V> implements KeyValueStore<K, V> {

    private final ExtractPayload extractPayload = new ExtractPayload();
    private final MapStore mapStore = new MapStore(new ExtractIndex(), new ExtractKey(), extractPayload);
    private final String pathToPartitions;
    private final int keySize;
    final int payloadSize;
    final int numPayloadsPerKey;
    private final int initialPageCapacity;
    private final Map<String, MapPage> indexPages;

    public KeyIndexer(String pathToPartitions,
        int keySize,
        int payloadSize,
        int numPayloadsPerKey,
        int maxOpenPartitions,
        int initialPageCapacity) {
        this.pathToPartitions = pathToPartitions;
        this.keySize = keySize;
        this.payloadSize = payloadSize;
        this.numPayloadsPerKey = numPayloadsPerKey;
        this.initialPageCapacity = initialPageCapacity;
        this.indexPages = Collections.synchronizedMap(new LruMap<String, MapPage>(maxOpenPartitions));
    }

    @Override
    public void add(K key, V value) throws KeyValueStoreException {
        if (key == null || value == null) {
            return;
        }

        byte[] keyBytes = keyBytes(key);
        byte[] rawChildActivity = valueBytes(value);
        if (rawChildActivity == null) {
            return;
        }
        synchronized (intern(key)) {
            MapPage index = index(key);
            byte[] payload = mapStore.get(index, keyBytes, extractPayload);
            if (payload == null) {
                payload = new byte[(payloadSize + 1) * numPayloadsPerKey];
            }
            boolean added = false;
            byte first = payload[0];
            while (!added) {
                for (int i = 0; i < numPayloadsPerKey; i++) {
                    int startIndex = i * (1 + payloadSize);
                    if (payload[startIndex] == 0) {
                        payload[startIndex] = 1;
                        System.arraycopy(rawChildActivity, 0, payload, startIndex + 1, rawChildActivity.length);
                        added = true;
                        break;
                    } else {
                        if (payload[startIndex] != first) {
                            payload[startIndex] = (byte) ((payload[startIndex] == 1) ? 2 : 1);
                            System.arraycopy(rawChildActivity, 0, payload, startIndex + 1, rawChildActivity.length);
                            added = true;
                            break;
                        }
                    }
                }
                first = (byte) ((first == 1) ? 2 : 1);
            }

            // TODO: this needs some major cleanup please!!!!
            try {
                if (mapStore.getCount(index) >= index.maxCount) {
                    int newSize = index.maxCount * 2;

                    File temporaryNewKeyIndexParition = createIndexTempFile(key);
                    MapPage newIndex = mmap(temporaryNewKeyIndexParition, newSize);
                    mapStore.copyTo(index, newIndex);
                    // TODO: implement to clean up
                    //index.close();
                    //newIndex.close();
                    File createIndexSetFile = createIndexSetFile(key);
                    FileUtils.forceDelete(createIndexSetFile);
                    FileUtils.copyFile(temporaryNewKeyIndexParition, createIndexSetFile);
                    FileUtils.forceDelete(temporaryNewKeyIndexParition);

                    index = mmap(createIndexSetFile(key), newSize);

                    indexPages.put(keyPartition(key), index);
                }
            } catch (IOException e) {
                throw new KeyValueStoreException("Error when expanding size of partition!", e);
            }

            mapStore.add(index, (byte) 1, keyBytes, payload);
        }

    }

    @Override
    public void remove(K key) throws KeyValueStoreException {
        if (key == null) {
            return;
        }

        byte[] keyBytes = keyBytes(key);
        synchronized (intern(key)) {
            MapPage index = index(key);
            mapStore.remove(index, keyBytes);
        }
    }

    private File createIndexSetFile(K key) {
        return createIndexFilePostfixed(keyPartition(key), ".set");
    }

    File createIndexSetFile(String partition) {
        return createIndexFilePostfixed(partition, ".set");
    }

    private File createIndexTempFile(K key) {
        return createIndexFilePostfixed(keyPartition(key) + "-" + UUID.randomUUID().toString(), ".tmp");
    }

    private File createIndexFilePostfixed(String partition, String postfix) {
        String newIndexFilename = partition + (postfix == null ? "" : postfix);
        return new File(pathToPartitions, newIndexFilename);
    }

    @Override
    public V get(K key) throws KeyValueStoreException {
        if (key == null) {
            return getNullObject();
        }
        MapPage index = index(key);
        byte[] keyBytes = keyBytes(key);
        byte[] payload;
        synchronized (intern(key)) {
            payload = mapStore.get(index, keyBytes, extractPayload);
        }
        if (payload == null) {
            return getNullObject();
        }

        return bytesValue(key, payload, 1);
    }

    protected int partitionCount() {
        return indexPages.size();
    }

    @Override
    public long estimatedMaxNumberOfKeys() {
        return FileUtils.sizeOfDirectory(new File(pathToPartitions)) / payloadSize;
    }

    protected abstract V getNullObject();

    private MapPage index(K key) throws KeyValueStoreException {
        try {
            String pageId = keyPartition(key);
            MapPage got = indexPages.get(pageId);
            if (got != null) {
                return got;
            }

            synchronized (indexPages) {
                got = indexPages.get(pageId);
                if (got != null) {
                    return got;
                }

                File file = createIndexSetFile(key);
                if (!file.exists()) {
                    // initializing in a temporary file prevents accidental corruption if the thread dies during mmap
                    File temporaryNewKeyIndexParition = createIndexTempFile(key);
                    MapPage newIndex = mmap(temporaryNewKeyIndexParition, initialPageCapacity);

                    File createIndexSetFile = createIndexSetFile(key);
                    FileUtils.copyFile(temporaryNewKeyIndexParition, createIndexSetFile);
                    FileUtils.forceDelete(temporaryNewKeyIndexParition);
                }

                got = mmap(file, initialPageCapacity);
                indexPages.put(pageId, got);
            }
            return got;
        } catch (FileNotFoundException fnfx) {
            throw new KeyValueStoreException("Page file could not be found", fnfx);
        } catch (IOException iox) {
            throw new KeyValueStoreException("Failed to map page from disk", iox);
        }
    }

    private MapPage mmap(final File file, final int maxCapacity) throws FileNotFoundException, IOException {

        if (file.exists()) {
            try {
                MappedByteBuffer buf;
                try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                    raf.seek(0);
                    FileChannel channel = raf.getChannel();
                    buf = channel.map(FileChannel.MapMode.READ_WRITE, 0, (int) channel.size());
                }

                MapPage page = new MapPage(new ByteBufferPage(buf));
                page.init();

                return page;
            } catch (IOException e) {
                //log.error("Failed to map existing file channel to byte buffer", e);
                e.printStackTrace();
                return null;
            }
        }

        MapPage set = mapStore.allocate((byte) 0, (byte) 0, new byte[16], 0, maxCapacity, keySize,
            (1 + payloadSize) * numPayloadsPerKey,
            new FileBackedMemMappedByteBufferPageFactory(file));

        return set;

    }

    private String intern(K key) {
        //todo this should be more intelligent
        return keyPartition(key).intern();
    }
}
