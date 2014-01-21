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

/**
 * Thrown by a RowColumnValueStore implementation when an exception occurs invoking a callback stream. This is used to differentiate between exceptions calling
 * the underlying store and exceptions in application code injected via the callback.
 *
 */
public class CallbackStreamException extends RuntimeException {

    public CallbackStreamException() {
    }

    public CallbackStreamException(String message) {
        super(message);
    }

    public CallbackStreamException(String message, Throwable cause) {
        super(message, cause);
    }

    public CallbackStreamException(Throwable cause) {
        super(cause);
    }

    public CallbackStreamException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
