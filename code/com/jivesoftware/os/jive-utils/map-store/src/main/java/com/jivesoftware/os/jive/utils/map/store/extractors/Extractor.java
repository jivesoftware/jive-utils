package com.jivesoftware.os.jive.utils.map.store.extractors;

import com.jivesoftware.os.jive.utils.map.store.MapPage;

/**
 *
 * @param <R>
 */
public interface Extractor<R> {

    R extract(int i, long _startIndex, int _keySize, int _payloadSize, MapPage page);

    R ifNull();

}
