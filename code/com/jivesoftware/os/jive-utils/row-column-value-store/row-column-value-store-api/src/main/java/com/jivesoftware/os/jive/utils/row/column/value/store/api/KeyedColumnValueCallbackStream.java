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

import com.jivesoftware.os.jive.utils.base.interfaces.CallbackStream;


/**
 * Helper class to handle bulk operations in RowColumnValueStore
 *
 */
public class KeyedColumnValueCallbackStream<R, C, V, TS> {

    private final R key;
    private final CallbackStream<ColumnValueAndTimestamp<C, V, TS>> callbackStream;

    public KeyedColumnValueCallbackStream(R key, CallbackStream<ColumnValueAndTimestamp<C, V, TS>> callbackStream) {
        this.key = key;
        this.callbackStream = callbackStream;
    }

    public R getKey() {
        return key;
    }

    public CallbackStream<ColumnValueAndTimestamp<C, V, TS>> getCallbackStream() {
        return callbackStream;
    }

    @Override
    public String toString() {
        return "KeyedColumnValueCallbackStream{"
            + "key=" + KeyToStringUtils.keyToString(key)
            + ", callbackStream=" + callbackStream + '}';
    }
}
