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
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.Timestamper;
import java.util.List;
import java.util.Map;

public class MasterSlaveHASetOfSortedMaps<T, R, C, V, E extends Exception> implements RowColumnValueStore<T, R, C, V, E> {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();

    public static enum ReadFailureMode {

        off, failToSlave, failToMaster
    }
    private final RowColumnValueStore<T, R, C, V, E> master;
    private final ReadFailureMode readFailureMode;
    private final RowColumnValueStore<T, R, C, V, E> readMaster;
    private final RowColumnValueStore<T, R, C, V, E> readSlave;

    public MasterSlaveHASetOfSortedMaps(RowColumnValueStore<T, R, C, V, E> master, ReadFailureMode readFailureMode, RowColumnValueStore<T, R, C, V, E> slave) {
        this.master = master;
        this.readFailureMode = readFailureMode;

        switch (readFailureMode) {
            case failToSlave:
                this.readMaster = master;
                this.readSlave = slave;
                break;
            case failToMaster:
                this.readMaster = slave;
                this.readSlave = master;
                break;
            default:
                this.readMaster = master;
                this.readSlave = null;
        }
    }

    @Override
    public void add(T tenantId, R rowKey, C columnKey, V columnValue, Integer timeToLiveInSeconds, Timestamper overrideTimestamper) throws E {
        master.add(tenantId, rowKey, columnKey, columnValue, timeToLiveInSeconds, overrideTimestamper);
    }

    @Override
    public boolean addIfNotExists(T tenantId, R rowKey, C columnKey, V columnValue, Integer timeToLiveInSeconds, Timestamper overrideTimestamper) throws E {
        return master.addIfNotExists(tenantId, rowKey, columnKey, columnValue, timeToLiveInSeconds, overrideTimestamper);
    }

    @Override
    public void multiAdd(T tenantId, R rowKey, C[] columnKeys, V[] columnValues, Integer timeToLiveInSeconds, Timestamper overrideTimestamper) throws E {
        master.multiAdd(tenantId, rowKey, columnKeys, columnValues, timeToLiveInSeconds, overrideTimestamper);
    }

    @Override
    public void multiRowsMultiAdd(T tenantId, List<RowColumValueTimestampAdd<R, C, V>> multiAdd) throws E {
        master.multiRowsMultiAdd(tenantId, multiAdd);
    }

    @Override
    public void multiRowsMultiAdd(List<TenantRowColumValueTimestampAdd<T, R, C, V>> multiAdd) throws E {
        master.multiRowsMultiAdd(multiAdd);
    }

    @Override
    public V get(T tenantId, R rowKey, C key, Integer overrideNumberOfRetries, Integer overrideConsistency) throws E {
        try {
            return readMaster.get(tenantId, rowKey, key, overrideNumberOfRetries, overrideConsistency);
        } catch (Throwable t) {
            LOG.warn("failing over get.", t);
            return readSlave.get(tenantId, rowKey, key, overrideNumberOfRetries, overrideConsistency);
        }
    }

    @Override
    public List<V> multiGet(T tenantId, R rowKey, C[] keys, Integer overrideNumberOfRetries, Integer overrideConsistency) throws E {
        try {
            return readMaster.multiGet(tenantId, rowKey, keys, overrideNumberOfRetries, overrideConsistency);
        } catch (Throwable t) {
            LOG.warn("failing over multiGet.", t);
            return readSlave.multiGet(tenantId, rowKey, keys, overrideNumberOfRetries, overrideConsistency);
        }
    }

    @Override
    public ColumnValueAndTimestamp<C, V, Long>[] multiGetEntries(
            T tenantId, R rowKey, C[] columnKeys, Integer overrideNumberOfRetries, Integer overrideConsistency) throws E {
        try {
            return readMaster.multiGetEntries(tenantId, rowKey, columnKeys, overrideNumberOfRetries, overrideConsistency);
        } catch (Throwable t) {
            LOG.warn("failing over multiGet.", t);
            return readSlave.multiGetEntries(tenantId, rowKey, columnKeys, overrideNumberOfRetries, overrideConsistency);
        }
    }

    @Override
    public void remove(T tenantId, R rowKey, C key, Timestamper overrideTimestamper) throws E {
        master.remove(tenantId, rowKey, key, overrideTimestamper);
    }

    @Override
    public void multiRemove(T tenantId, R rowKey, C[] key, Timestamper overrideTimestamper) throws E {
        master.multiRemove(tenantId, rowKey, key, overrideTimestamper);
    }

    @Override
    public void multiRowsMultiRemove(T tenantId, List<RowColumnTimestampRemove<R, C>> multiRemove) throws E {
        master.multiRowsMultiRemove(tenantId, multiRemove);
    }

    @Override
    public void multiRowsMultiRemove(List<TenantRowColumnTimestampRemove<T, R, C>> multiRemove) throws E {
        master.multiRowsMultiRemove(multiRemove);
    }

    @Override
    public void getKeys(T tenantId, R rowKey, C start, Long maxCount, int batchSize,
    boolean reversed, Integer overrideNumberOfRetries, Integer overrideConsistency, CallbackStream<C> callback) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void getValues(T tenantId, R rowKey, C start, Long maxCount, int batchSize,
    boolean reversed, Integer overrideNumberOfRetries, Integer overrideConsistency, CallbackStream<V> callback) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <TS> void getEntrys(T tenantId, R rowKey, C start, Long maxCount, int batchSize,
    boolean reversed, Integer overrideNumberOfRetries, Integer overrideConsistency, CallbackStream<ColumnValueAndTimestamp<C, V, TS>> callback) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void getAllRowKeys(int batchSize, Integer overrideNumberOfRetries, CallbackStream<TenantIdAndRow<T, R>> callback) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeRow(T tenantId, R rowKey, Timestamper overrideTimestamper) throws E {
        master.removeRow(tenantId, rowKey, overrideTimestamper);
    }

    @Override
    public List<V> multiRowGet(T tenantId, List<R> rowKeys, C columnKey, Integer overrideNumberOfRetries, Integer overrideConsistency) throws E {
        try {
            return readMaster.multiRowGet(tenantId, rowKeys, columnKey, overrideNumberOfRetries, overrideConsistency);
        } catch (Throwable t) {
            LOG.warn("failing over multiGet.", t);
            return readSlave.multiRowGet(tenantId, rowKeys, columnKey, overrideNumberOfRetries, overrideConsistency);
        }
    }

    @Override
    public List<Map<C, V>> multiRowMultiGet(T tenantId, List<R> rowKeys, List<C> columnKeys, Integer overrideNumberOfRetries, Integer overrideConsistency)
        throws E {
        try {
            return readMaster.multiRowMultiGet(tenantId, rowKeys, columnKeys, overrideNumberOfRetries, overrideConsistency);
        } catch (Throwable t) {
            LOG.warn("failing over multiRowMultiGet.", t);
            return readSlave.multiRowMultiGet(tenantId, rowKeys, columnKeys, overrideNumberOfRetries, overrideConsistency);
        }
    }

    @Override
    public <TS> void multiRowGetAll(T tenantId, List<KeyedColumnValueCallbackStream<R, C, V, TS>> rowKeyCallbackStreamPairs) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <TS> void multiRowGetAll(List<TenantKeyedColumnValueCallbackStream<T, R, C, V, TS>> tenantRowKeyCallbackStreamPairs) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
