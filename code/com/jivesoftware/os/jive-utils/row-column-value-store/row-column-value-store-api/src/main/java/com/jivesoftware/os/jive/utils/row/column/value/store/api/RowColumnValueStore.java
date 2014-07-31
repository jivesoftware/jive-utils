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
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.Timestamper;
import java.util.List;
import java.util.Map;

/**
 * Set of Sorted Sets
 *
 * @author jonathan
 * @param <T> tenantId
 * @param <R> Row
 * @param <C> Column
 * @param <V> Value
 */
public interface RowColumnValueStore<T, R, C, V, E extends Exception> {

    /**
     *
     * @param rowKey cannot be null
     * @param columnKey cannot be null
     * @param columnValue cannot be null
     * @param timeToLiveInSeconds use null to ignore
     * @throws Exception
     */
    void add(T tenantId, R rowKey, C columnKey, V columnValue, Integer timeToLiveInSeconds,
            Timestamper overrideTimestamper) throws E;

    /**
     *
     * @param rowKey cannot be null
     * @param columnKey cannot be null
     * @param columnValue cannot be null
     * @param timeToLiveInSeconds use null to ignore
     * @throws Exception
     */
    boolean addIfNotExists(T tenantId, R rowKey, C columnKey, V columnValue, Integer timeToLiveInSeconds,
            Timestamper overrideTimestamper) throws E;

    /**
     *
     * @param tenantId
     * @param rowKey
     * @param columnKey
     * @param columnValue
     * @param expectedValue
     * @param timeToLiveInSeconds
     * @param overrideTimestamper
     * @return
     * @throws E
     */
    boolean replaceIfEqualToExpected(T tenantId, R rowKey, C columnKey, V columnValue, V expectedValue,
            Integer timeToLiveInSeconds, Timestamper overrideTimestamper) throws E;

    /**
     *
     * @param rowKey cannot be null
     * @param columnKeys null not supported! keys.length must equal
     * values.length
     * @param columnValues null not supported! values.length must equal
     * keys.length
     * @param timeToLiveInSeconds use null to ignore
     */
    void multiAdd(T tenantId, R rowKey, C[] columnKeys, V[] columnValues, Integer timeToLiveInSeconds,
            Timestamper overrideTimestamper) throws E;

    /**
     *
     */
    void multiRowsMultiAdd(T tenantId, List<RowColumValueTimestampAdd<R, C, V>> multiAdd) throws E;

    /**
     *
     */
    void multiRowsMultiAdd(List<TenantRowColumValueTimestampAdd<T, R, C, V>> multiAdd) throws E;

    /**
     *
     * @param tenantId cannot be null
     * @param rowKey cannot be null
     * @param key cannot be null
     * @param overrideNumberOfRetries can be null
     * @param overrideConsistency can be null
     * @return
     */
    V get(T tenantId, R rowKey, C key, Integer overrideNumberOfRetries, Integer overrideConsistency) throws E;

    /**
     *
     * @param tenantId cannot be null
     * @param rowKey cannot be null
     * @param keys cannot be null
     * @param overrideNumberOfRetries
     * @param overrideConsistency
     * @return
     */
    List<V> multiGet(T tenantId, R rowKey, C[] keys, Integer overrideNumberOfRetries,
            Integer overrideConsistency) throws E;

    /**
     *
     * @param tenantId
     * @param rowKey
     * @param columnKeys
     * @param overrideNumberOfRetries
     * @param overrideConsistency
     * @return
     */
    ColumnValueAndTimestamp<C, V, Long>[] multiGetEntries(T tenantId,
            R rowKey, C[] columnKeys, Integer overrideNumberOfRetries, Integer overrideConsistency) throws E;

    /**
     *
     * @param rowKey cannot be null
     * @param key cannot be null
     * @throws Exception
     */
    void remove(T tenantId, R rowKey, C key, Timestamper overrideTimestamper) throws E;

    boolean removeIfEqualToExpected(T tenantId, R rowKey, C columnKey, V expectedValue,
            Timestamper overrideTimestamper) throws E;

    /**
     *
     * @param tenantId cannot be null
     * @param rowKey cannot be null
     * @param key cannot be null
     */
    void multiRemove(T tenantId, R rowKey, C[] key, Timestamper overrideTimestamper) throws E;

    /**
     *
     * @param tenantId cannot be null
     */
    void multiRowsMultiRemove(T tenantId, List<RowColumnTimestampRemove<R, C>> multiRemove) throws E;

    /**
     *
     * @param tenantId
     * @param multiRemove
     */
    void multiRowsMultiRemove(List<TenantRowColumnTimestampRemove<T, R, C>> multiRemove) throws E;

    /**
     *
     * @param rowKey
     * @param start
     * @param batchSize
     * @param reversed
     * @param callback
     * @throws Exception
     */
    void getKeys(T tenantId, R rowKey, C start, Long maxCount, int batchSize, boolean reversed,
            Integer overrideNumberOfRetries, Integer overrideConsistency, CallbackStream<C> callback) throws E;

    /**
     *
     * @param tenantId
     * @param rowKey
     * @param start
     * @param maxCount
     * @param batchSize
     * @param reversed
     * @param overrideNumberOfRetries
     * @param overrideConsistency
     * @param callback
     */
    void getValues(T tenantId, R rowKey, C start, Long maxCount, int batchSize, boolean reversed,
            Integer overrideNumberOfRetries, Integer overrideConsistency, CallbackStream<V> callback) throws E;

    /**
     *
     * @param <TS>
     * @param tenantId
     * @param rowKey
     * @param start
     * @param maxCount
     * @param batchSize
     * @param reversed
     * @param overrideNumberOfRetries
     * @param overrideConsistency
     * @param callback
     */
    <TS> void getEntrys(T tenantId, R rowKey, C start, Long maxCount, int batchSize, boolean reversed,
            Integer overrideNumberOfRetries, Integer overrideConsistency,
            CallbackStream<ColumnValueAndTimestamp<C, V, TS>> callback) throws E;

    /**
     *
     * @param batchSize
     * @param overrideNumberOfRetries
     * @param callback
     */
    void getAllRowKeys(int batchSize, Integer overrideNumberOfRetries,
            CallbackStream<TenantIdAndRow<T, R>> callback) throws E;

    /**
     *
     * @param tenantId
     * @param startRowKey
     * @param stopRowKey
     * @param batchSize
     * @param overrideNumberOfRetries
     * @param callback
     * @throws Exception
     */
    void getRowKeys(T tenantId, R startRowKey, R stopRowKey, int batchSize, Integer overrideNumberOfRetries, CallbackStream<TenantIdAndRow<T, R>> callback) throws E;

    /**
     *
     * @param tenantId
     * @param rowKey
     * @param overrideTimestamper
     */
    void removeRow(T tenantId, R rowKey, Timestamper overrideTimestamper) throws E;

    /**
     *
     * @param tenantId
     * @param rowKeys
     * @param columnKey
     * @param overrideNumberOfRetries
     * @param overrideConsistency
     * @return
     */
    List<V> multiRowGet(T tenantId, List<R> rowKeys, C columnKey, Integer overrideNumberOfRetries, Integer overrideConsistency) throws E;

    /**
     * Returns a List containing the values for specified columns for multiple rows (as a map from column to its value).
     * If a row does not exist, a null entry is inserted in the list instead. If there's no value for a specific column
     * and a specific row, there would be no entry in the corresponding map.
     *
     * @param tenantId
     * @param rowKeys
     * @param columnKeys
     * @param overrideNumberOfRetries
     * @param overrideConsistency
     * @return
     */
    List<Map<C, V>> multiRowMultiGet(T tenantId, List<R> rowKeys, List<C> columnKeys, Integer overrideNumberOfRetries, Integer overrideConsistency) throws E;

    /**
     *
     * @param <TS>
     * @param tenantId
     * @param rowKeyCallbackStreamPairs
     */
    <TS> void multiRowGetAll(T tenantId, List<KeyedColumnValueCallbackStream<R, C, V, TS>> rowKeyCallbackStreamPairs) throws E;

    /**
     *
     * @param <TS>
     * @param tenantId
     * @param rowKeyCallbackStreamPairs
     */
    <TS> void multiRowGetAll(List<TenantKeyedColumnValueCallbackStream<T, R, C, V, TS>> tenantRowKeyCallbackStreamPairs) throws E;
}
