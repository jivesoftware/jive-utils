package com.jivesoftware.os.jive.utils.map.store.extractors;

import com.jivesoftware.os.jive.utils.map.store.MapPage;

/**
 *
 * @author jonathan.colt
 */
public class ExtractIndex implements Extractor<Integer> {

    @Override
    public Integer extract(int i, long _startIndex, int _keySize, int _payloadSize, MapPage page) {
        return i;
    }

    @Override
    public Integer ifNull() {
        return -1;
    }

}
