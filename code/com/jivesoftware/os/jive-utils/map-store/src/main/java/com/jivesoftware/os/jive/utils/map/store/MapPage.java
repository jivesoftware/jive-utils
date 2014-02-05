package com.jivesoftware.os.jive.utils.map.store;

import com.jivesoftware.os.jive.utils.map.store.extractors.ExtractIndex;
import com.jivesoftware.os.jive.utils.map.store.extractors.ExtractKey;
import com.jivesoftware.os.jive.utils.map.store.extractors.ExtractPayload;
import com.jivesoftware.os.jive.utils.map.store.pages.ByteArrayPage;
import com.jivesoftware.os.jive.utils.map.store.pages.Page;
import com.jivesoftware.os.jive.utils.map.store.pages.PageProxy;

/**
 *
 * @author jonathan
 */
public class MapPage extends PageProxy {

    /**
     *
     */
    static final public MapPage cNull = new MapPage(new ByteArrayPage(new byte[0]));
    int keySize; // read only
    int payloadSize; // read only
    int capacity; // read only
    int maxCount; // read only

    /**
     *
     * @param page
     */
    public MapPage(Page page) {
        super(page);
    }

    /**
     *
     */
    @Override
    public void init() {
        super.init();
        MapStore pset = new MapStore(new ExtractIndex(), new ExtractKey(), new ExtractPayload());
        keySize = pset.getKeySize(this);
        payloadSize = pset.getPayloadSize(this);
        maxCount = pset.getMaxCount(this);
        capacity = pset.getCapacity(this);
    }
}
