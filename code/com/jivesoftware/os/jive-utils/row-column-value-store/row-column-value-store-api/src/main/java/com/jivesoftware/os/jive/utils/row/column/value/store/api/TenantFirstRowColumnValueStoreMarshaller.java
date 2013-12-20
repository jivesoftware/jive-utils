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

import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.api.TypeMarshaller;
import java.util.Arrays;

/**
 * Marshaller for an HBase table that stores the tenant ID at the front. The tenantId and row key are converted to lexicographic sortable bytes and combined
 * into a single byte array with the tenantId at the front. The column keys are also converted to lexicographic sortable bytes. Values are converted to just
 * normal bytes, not necessarily lexicographic sortable.
 *
 * Specifically, the row key bytes will end up looking as follows:
 *
 * [tenantId bytes][row key object bytes][length of tenantId bytes]
 *
 * where the length of the tenantId bytes is stored in a single byte. This then means that any tenantId string provided to this class MUST have a length of less
 * than or equal to 256 characters.
 *
 * @param <R> the type of objects that are used for the row key of the table
 * @param <C> the type of objects that are used for the column key of the table
 * @param <V> the type of objects that are used for the values in the table.
 */
public class TenantFirstRowColumnValueStoreMarshaller<T, R, C, V> implements RowColumnValueStoreMarshaller<T, R, C, V> {

    private static final int MAX_TENANT_ID_STRING_LENGTH = 256;

    private final TypeMarshaller<T> tenantIdMarshaller;
    private final TypeMarshaller<R> rowKeyMarshaller;
    private final TypeMarshaller<C> columnKeyMarshaller;
    private final TypeMarshaller<V> valueMarshaller;

    public TenantFirstRowColumnValueStoreMarshaller(TypeMarshaller<T> tenantIdMarshaller,
            TypeMarshaller<R> rowKeyMarshaller,
            TypeMarshaller<C> columnKeyMarshaller,
            TypeMarshaller<V> valueMarshaller) {
        this.tenantIdMarshaller = tenantIdMarshaller;
        this.rowKeyMarshaller = rowKeyMarshaller;
        this.columnKeyMarshaller = columnKeyMarshaller;
        this.valueMarshaller = valueMarshaller;
    }

    @Override
    public byte[] toRowKeyBytes(T tenantId, R rowKey) throws RowColumnValueStoreMarshallerException {
        try {
            byte[] tenantIdBytes = tenantIdMarshaller.toLexBytes(tenantId);
            if (tenantIdBytes.length > MAX_TENANT_ID_STRING_LENGTH) {
                throw new IllegalArgumentException("tenantId (" + tenantId + ") is too long.  Max is "
                        + MAX_TENANT_ID_STRING_LENGTH + " bytes.");
            }

            byte[] rawKey = rowKeyMarshaller.toLexBytes(rowKey);

            return join(tenantIdBytes, rawKey, new byte[]{(byte) tenantIdBytes.length});
        } catch (Exception e) {
            throw new RowColumnValueStoreMarshallerException("Error converting tenantId and rowKey to bytes ("
                    + tenantId + ", " + rowKey + ")", e);
        }
    }

    byte[] join(byte[] a, byte[] b, byte[] c) {
        byte[] newSrc = new byte[a.length + b.length + c.length];
        System.arraycopy(a, 0, newSrc, 0, a.length);
        System.arraycopy(b, 0, newSrc, a.length, b.length);
        System.arraycopy(c, 0, newSrc, a.length + b.length, c.length);
        return newSrc;
    }

    @Override
    public TenantIdAndRow<T, R> fromRowKeyBytes(byte[] rowKeyBytes) throws RowColumnValueStoreMarshallerException {
        try {
            int tenantIdSize = rowKeyBytes[rowKeyBytes.length - 1];
            byte[] tenantIdBytes = Arrays.copyOfRange(rowKeyBytes, 0, tenantIdSize);

            T tenantId = tenantIdMarshaller.fromLexBytes(tenantIdBytes);

            byte[] tenantIdRemovedRowKeyBytes = Arrays.copyOfRange(rowKeyBytes, tenantIdSize, rowKeyBytes.length - 1);
            R rowKey = rowKeyMarshaller.fromLexBytes(tenantIdRemovedRowKeyBytes);

            return new TenantIdAndRow<>(tenantId, rowKey);
        } catch (Exception e) {
            throw new RowColumnValueStoreMarshallerException("Error extracting tenantId and rowKey from bytes", e);
        }
    }

    @Override
    public byte[] toColumnKeyBytes(C columnKey) throws RowColumnValueStoreMarshallerException {
        try {
            return columnKeyMarshaller.toLexBytes(columnKey);
        } catch (Exception e) {
            throw new RowColumnValueStoreMarshallerException("Error converting columnKey to bytes (" + columnKey + ")", e);
        }
    }

    @Override
    public C fromColumnKeyBytes(byte[] columnKeyBytes) throws RowColumnValueStoreMarshallerException {
        try {
            return columnKeyMarshaller.fromLexBytes(columnKeyBytes);
        } catch (Exception e) {
            throw new RowColumnValueStoreMarshallerException("Error extracting columnKey from bytes", e);
        }
    }

    @Override
    public byte[] toValueBytes(V value) throws RowColumnValueStoreMarshallerException {
        try {
            return valueMarshaller.toBytes(value);
        } catch (Exception e) {
            throw new RowColumnValueStoreMarshallerException("Error converting value to bytes (" + value + ")", e);
        }
    }

    @Override
    public V fromValueBytes(byte[] valueBytes) throws RowColumnValueStoreMarshallerException {
        try {
            return valueMarshaller.fromBytes(valueBytes);
        } catch (Exception e) {
            throw new RowColumnValueStoreMarshallerException("Error extracting value from bytes", e);
        }
    }
}
