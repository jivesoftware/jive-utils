package com.jivesoftware.os.jive.utils.map.store.extractors;

/**
 *
 * @author jonathan.colt
 * @param <R>
 */
public interface ExtractorStream<R> {

    public <R> R stream(R v);

}
