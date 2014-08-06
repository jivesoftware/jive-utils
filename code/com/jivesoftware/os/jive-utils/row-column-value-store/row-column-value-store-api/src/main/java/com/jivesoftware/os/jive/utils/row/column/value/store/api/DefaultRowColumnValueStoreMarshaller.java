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
 * Default marshaller for an HBase table. The tenantId and row key are converted to lexicographic sortable bytes and combined into a single byte array with the
 * tenantId at the back. The column keys are also converted to lexicographic sortable bytes. Values are converted to just normal bytes, not necessarily
 * lexicographic sortable.
 *
 * Specifically, the row key bytes will end up looking as follows:
 *
 * [row key object bytes][tenantId bytes][length of tenantId bytes]
 *
 * where the length of the tenantId bytes is stored in a single byte. This then means that any tenantId string provided to this class MUST have a length of less
 * than or equal to 256 characters.
 *
 * @param <R> the type of objects that are used for the row key of the table
 * @param <C> the type of objects that are used for the column key of the table
 * @param <V> the type of objects that are used for the values in the table.
 */
public class DefaultRowColumnValueStoreMarshaller<T, R, C, V> implements RowColumnValueStoreMarshaller<T, R, C, V> {

    private static final int MAX_TENANT_ID_STRING_LENGTH = 256;

    private final TypeMarshaller<R> rowKeyMarshaller;
    private final TypeMarshaller<C> columnKeyMarshaller;
    private final TypeMarshaller<V> valueMarshaller;
    private final TypeMarshaller<T> tenantKeyMarshaller;

    public DefaultRowColumnValueStoreMarshaller(TypeMarshaller<T> tenantKeyMarshaller,
            TypeMarshaller<R> rowKeyMarshaller,
            TypeMarshaller<C> columnKeyMarshaller,
            TypeMarshaller<V> valueMarshaller) {
        this.tenantKeyMarshaller = tenantKeyMarshaller;
        this.rowKeyMarshaller = rowKeyMarshaller;
        this.columnKeyMarshaller = columnKeyMarshaller;
        this.valueMarshaller = valueMarshaller;
    }

    @Override
    public byte[] toRowKeyBytes(T tenantId, R rowKey) throws RowColumnValueStoreMarshallerException {
        if (tenantId.toString().length() > MAX_TENANT_ID_STRING_LENGTH) {
            throw new IllegalArgumentException("tenantId string (" + tenantId + ") is too long.  Max is "
                    + MAX_TENANT_ID_STRING_LENGTH + " characters.");
        }

        try {
            byte[] tenantIdBytes = tenantKeyMarshaller.toLexBytes(tenantId);

            byte[] tenantIdPlusSizeAtTail = addSizeToTail(tenantIdBytes);
            byte[] rawKey = rowKeyMarshaller.toLexBytes(rowKey);

            return join(rawKey, tenantIdPlusSizeAtTail);
        } catch (Exception e) {
            throw new RowColumnValueStoreMarshallerException("Error converting tenantId and rowKey to bytes ("
                    + tenantId + ", " + rowKey + ")", e);
        }
    }

    byte[] join(byte[] a, byte[] b) {
        byte[] newSrc = new byte[a.length + b.length];
        System.arraycopy(a, 0, newSrc, 0, a.length);
        System.arraycopy(b, 0, newSrc, a.length, b.length);
        return newSrc;
    }

    private byte[] addSizeToTail(byte[] bytes) throws Exception {
        return push(bytes, (byte) bytes.length);
    }

    byte[] push(byte[] src, byte instance) {
        if (src == null) {
            src = new byte[0];
        }
        byte[] newSrc = new byte[src.length + 1];
        System.arraycopy(src, 0, newSrc, 0, src.length);
        newSrc[src.length] = instance;
        return newSrc;
    }

    @Override
    public TenantIdAndRow<T, R> fromRowKeyBytes(byte[] rowKeyBytes) throws RowColumnValueStoreMarshallerException {
        try {
            int tenantIdSize = extractSizeFromTail(rowKeyBytes);
            // The extra -1 here is necessary because the last byte is the size of the tenantId string
            int tenantIdIndexStart = rowKeyBytes.length - 1 - tenantIdSize;
            byte[] tenantIdBytes = Arrays.copyOfRange(rowKeyBytes, tenantIdIndexStart, tenantIdIndexStart + tenantIdSize);

            T tenantId = tenantKeyMarshaller.fromLexBytes(tenantIdBytes);
            byte[] tenantIdRemovedRowKeyBytes = Arrays.copyOfRange(rowKeyBytes, 0, tenantIdIndexStart);
            R rowKey = rowKeyMarshaller.fromLexBytes(tenantIdRemovedRowKeyBytes);

            return new TenantIdAndRow<>(tenantId, rowKey);
        } catch (Exception e) {
            throw new RowColumnValueStoreMarshallerException("Error extracting tenantId and rowKey from bytes", e);
        }
    }

    private int extractSizeFromTail(byte[] bytes) {
        return (int) bytes[bytes.length - 1];
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
            throw new RowColumnValueStoreMarshallerException("Error extracting value "
                    + "from bytes using " + valueMarshaller.getClass() + " bytes.length=" + valueBytes.length, e);
        }
    }
}
