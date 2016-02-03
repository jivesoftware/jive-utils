package com.jivesoftware.os.jive.utils.collections;

/**
 *
 * @author jonathan.colt
 */
public interface KeyValueStream<K, V> {

    boolean keyValue(K key, V value) throws Exception;

}
