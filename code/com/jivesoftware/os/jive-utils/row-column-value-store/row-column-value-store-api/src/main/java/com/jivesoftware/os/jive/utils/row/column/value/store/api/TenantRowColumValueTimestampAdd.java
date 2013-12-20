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

/**
 *
 */
public class TenantRowColumValueTimestampAdd<T, R, C, V> {

    private final T tenantId;
    private final R rowKey;
    private final C columnKey;
    private final V columnValue;
    private final Timestamper overrideTimestamper;

    public TenantRowColumValueTimestampAdd(T tenantId, R rowKey, C columnKey, V columnValue, Timestamper overrideTimestamper) {
        this.tenantId = tenantId;
        this.rowKey = rowKey;
        this.columnKey = columnKey;
        this.columnValue = columnValue;
        this.overrideTimestamper = overrideTimestamper;
    }

    public T getTenantId() {
        return tenantId;
    }

    public R getRowKey() {
        return rowKey;
    }

    public C getColumnKey() {
        return columnKey;
    }

    public V getColumnValue() {
        return columnValue;
    }

    public Timestamper getOverrideTimestamper() {
        return overrideTimestamper;
    }

    @Override
    public String toString() {
        return "TenantRowColumValueTimestampAdd{"
            + "tenantId=" + tenantId
            + ", rowKey=" + KeyToStringUtils.keyToString(rowKey)
            + ", columnKey=" + KeyToStringUtils.keyToString(columnKey)
            + ", columnValue=" + columnValue
            + ", overrideTimestamper=" + overrideTimestamper + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (this.tenantId != null ? this.tenantId.hashCode() : 0);
        hash = 23 * hash + (this.rowKey != null ? this.rowKey.hashCode() : 0);
        hash = 23 * hash + (this.columnKey != null ? this.columnKey.hashCode() : 0);
        hash = 23 * hash + (this.columnValue != null ? this.columnValue.hashCode() : 0);
        hash = 23 * hash + (this.overrideTimestamper != null ? this.overrideTimestamper.hashCode() : 0);
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
        @SuppressWarnings ("unchecked")
        final TenantRowColumValueTimestampAdd<T, R, C, V> other = (TenantRowColumValueTimestampAdd<T, R, C, V>) obj;
        if (this.tenantId != other.tenantId && (this.tenantId == null || !this.tenantId.equals(other.tenantId))) {
            return false;
        }
        if (this.rowKey != other.rowKey && (this.rowKey == null || !this.rowKey.equals(other.rowKey))) {
            return false;
        }
        if (this.columnKey != other.columnKey && (this.columnKey == null || !this.columnKey.equals(other.columnKey))) {
            return false;
        }
        if (this.columnValue != other.columnValue && (this.columnValue == null || !this.columnValue.equals(other.columnValue))) {
            return false;
        }
        if (this.overrideTimestamper != other.overrideTimestamper
            && (this.overrideTimestamper == null || !this.overrideTimestamper.equals(other.overrideTimestamper))) {
            return false;
        }
        return true;
    }
}
