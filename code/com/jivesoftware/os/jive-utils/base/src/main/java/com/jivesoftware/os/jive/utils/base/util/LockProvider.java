/*
 * Copyright 2013 Jive Software, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
