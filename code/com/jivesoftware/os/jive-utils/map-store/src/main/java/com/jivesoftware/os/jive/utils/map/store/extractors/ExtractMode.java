package com.jivesoftware.os.jive.utils.map.store.extractors;

import com.jivesoftware.os.jive.utils.map.store.MapPage;

/**
 *
 * @author jonathan.colt
 */
public class ExtractMode implements Extractor<Byte> {

    @Override
    public Byte extract(int i, long _startIndex, int _keySize, int _payloadSize, MapPage page) {
        return page.read((int) _startIndex);
    }

    @Override
    public Byte ifNull() {
        return null;
    }

}
