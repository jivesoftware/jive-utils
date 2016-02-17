package com.jivesoftware.os.jive.utils.collections.lh;

/**
 *
 * @author jonathan.colt
 */
public interface LHashValueStream<V> {

    boolean keyValue(long key, V value) throws Exception;

}
