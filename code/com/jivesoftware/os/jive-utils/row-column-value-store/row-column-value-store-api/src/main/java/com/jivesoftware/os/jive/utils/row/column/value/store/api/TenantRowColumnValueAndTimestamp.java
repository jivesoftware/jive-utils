/*
 * Copyright 2014 Jive Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jivesoftware.os.jive.utils.row.column.value.store.api;

import java.util.Objects;

/**
 *
 * @author pete
 */
public class TenantRowColumnValueAndTimestamp<T, R, C, V, TS> {

    private final T tenant;
    private final R row;
    private final C column;
    private final V value;
    private final TS timestamp;

    public TenantRowColumnValueAndTimestamp(T tenant, R row, C column, V value, TS timestamp) {
        this.tenant = tenant;
        this.row = row;
        this.column = column;
        this.value = value;
        this.timestamp = timestamp;
    }
    
    public T getTenant() {
        return tenant;
    }

    public R getRow() {
        return row;
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
            + "tenant=" + tenant
            + "row=" + KeyToStringUtils.keyToString(row)
            + "column=" + KeyToStringUtils.keyToString(column)
            + ", value=" + value
            + ", timestamp=" + timestamp + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + Objects.hashCode(this.tenant);
        hash = 13 * hash + Objects.hashCode(this.row);
        hash = 13 * hash + Objects.hashCode(this.column);
        hash = 13 * hash + Objects.hashCode(this.value);
        hash = 13 * hash + Objects.hashCode(this.timestamp);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TenantRowColumnValueAndTimestamp<T, R, C, V, TS> other = (TenantRowColumnValueAndTimestamp<T, R, C, V, TS>) obj;
        if (!Objects.equals(this.tenant, other.tenant)) {
            return false;
        }
        if (!Objects.equals(this.row, other.row)) {
            return false;
        }
        if (!Objects.equals(this.column, other.column)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        if (!Objects.equals(this.timestamp, other.timestamp)) {
            return false;
        }
        return true;
    }

}
