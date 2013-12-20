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
package com.jivesoftware.os.jive.utils.row.column.value.store.api;

import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.Timestamper;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Can result in adding the same value twice.
 */
public class MultiAdd<R, C, V> {

    private final LinkedBlockingQueue<RowColumValueTimestampAdd<R, C, V>> queue = new LinkedBlockingQueue<>();

    public MultiAdd() {
    }

    public void add(R rowKeys, C columnKeys, V columnValues, Timestamper overrideTimestamper) {
        boolean added = queue.offer(new RowColumValueTimestampAdd<>(rowKeys, columnKeys, columnValues, overrideTimestamper));
        while (!added) {
            added = queue.offer(new RowColumValueTimestampAdd<>(rowKeys, columnKeys, columnValues, overrideTimestamper));
        }
    }

    public List<RowColumValueTimestampAdd<R, C, V>> take() {
        LinkedList<RowColumValueTimestampAdd<R, C, V>> took = new LinkedList<>();
        queue.drainTo(took);
        return took;
    }

    public int size() {
        return queue.size();
    }
}
