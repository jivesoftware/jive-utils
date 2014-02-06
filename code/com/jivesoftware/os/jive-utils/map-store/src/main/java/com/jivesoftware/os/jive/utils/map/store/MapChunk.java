package com.jivesoftware.os.jive.utils.map.store;

import com.jivesoftware.os.jive.utils.map.store.extractors.ExtractIndex;
import com.jivesoftware.os.jive.utils.map.store.extractors.ExtractKey;
import com.jivesoftware.os.jive.utils.map.store.extractors.ExtractPayload;
import com.jivesoftware.os.jive.utils.map.store.pages.ByteBufferChunk;
import com.jivesoftware.os.jive.utils.map.store.pages.Chunk;
import com.jivesoftware.os.jive.utils.map.store.pages.ChunkProxy;
import java.nio.ByteBuffer;

/**
 *
 * @author jonathan
 */
public class MapChunk extends ChunkProxy {

    /**
     *
     */
    int keySize; // read only
    int payloadSize; // read only
    int capacity; // read only
    int maxCount; // read only

    /**
     *
     * @param page
     */
    public MapChunk(Chunk page) {
        super(page);
    }

    /**
     *
     */
    @Override
    public void init() {
        super.init();
        MapStore mapStore = new MapStore(new ExtractIndex(), new ExtractKey(), new ExtractPayload());
        keySize = mapStore.getKeySize(this);
        payloadSize = mapStore.getPayloadSize(this);
        maxCount = mapStore.getMaxCount(this);
        capacity = mapStore.getCapacity(this);
    }
}
