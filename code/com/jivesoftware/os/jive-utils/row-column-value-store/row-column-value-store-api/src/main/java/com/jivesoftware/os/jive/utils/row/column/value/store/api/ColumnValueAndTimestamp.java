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
 *
 * @author jonathan
 */
public class ColumnValueAndTimestamp<C, V, TS> {

    private final C column;
    private final V value;
    private final TS timestamp;

    public ColumnValueAndTimestamp(C column, V value, TS timestamp) {
        this.column = column;
        this.value = value;
        this.timestamp = timestamp;
    }

    public C getColumn() {
        return column;
    }

    public TS getTimestamp() {
        return timestamp;
    }

    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "ColumnValueAndTimestamp{"
            + "column=" + KeyToStringUtils.keyToString(column)
            + ", value=" + value
            + ", timestamp=" + timestamp + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ColumnValueAndTimestamp<C, V, TS> other = (ColumnValueAndTimestamp<C, V, TS>) obj;
        if (this.column != other.column && (this.column == null || !this.column.equals(other.column))) {
            return false;
        }
        if (this.value != other.value && (this.value == null || !this.value.equals(other.value))) {
            return false;
        }
        if (this.timestamp != other.timestamp && (this.timestamp == null || !this.timestamp.equals(other.timestamp))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (this.column != null ? this.column.hashCode() : 0);
        hash = 71 * hash + (this.value != null ? this.value.hashCode() : 0);
        hash = 71 * hash + (this.timestamp != null ? this.timestamp.hashCode() : 0);
        return hash;
    }
}
