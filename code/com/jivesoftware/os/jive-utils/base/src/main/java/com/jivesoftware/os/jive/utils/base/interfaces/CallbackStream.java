package com.jivesoftware.os.jive.utils.base.interfaces;

public interface CallbackStream<V> {

    public V callback(V value) throws Exception;
}
