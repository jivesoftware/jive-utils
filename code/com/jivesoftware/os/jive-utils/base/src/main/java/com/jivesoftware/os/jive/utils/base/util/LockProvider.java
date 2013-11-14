/*
 * $Revision: 142270 $
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.base.util;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author jonathan
 */
public class LockProvider<K> {

    final private ConcurrentHashMap<K, Object> locks = new ConcurrentHashMap<>();

    public LockProvider() {
    }

    public Object getLock(K key) {
        Object lock = locks.get(key);
        if (lock == null) {
            lock = new Object();
            Object had = locks.putIfAbsent(key, lock);
            if (had != null) {
                lock = had;
            }
        }
        return lock;
    }
}
