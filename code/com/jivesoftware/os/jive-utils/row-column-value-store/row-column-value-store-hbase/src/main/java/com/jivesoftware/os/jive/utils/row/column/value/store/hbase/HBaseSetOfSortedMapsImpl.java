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
package com.jivesoftware.os.jive.utils.row.column.value.store.hbase;

import com.google.common.collect.Lists;
import com.jivesoftware.os.jive.utils.base.interfaces.CallbackStream;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.CallbackStreamException;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.ColumnValueAndTimestamp;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.KeyedColumnValueCallbackStream;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumValueTimestampAdd;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnTimestampRemove;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStoreMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStoreMarshallerException;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.TenantIdAndRow;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.TenantKeyedColumnValueCallbackStream;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.TenantRowColumValueTimestampAdd;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.TenantRowColumnTimestampRemove;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.ValueStoreMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.Timestamper;
import com.jivesoftware.os.jive.utils.row.column.value.store.shared.RowColumnValueStoreCounters;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.apache.commons.lang.mutable.MutableLong;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.ColumnRangeFilter;
import org.apache.hadoop.hbase.filter.Filter;

/**
 * HBase implementation of RowColumnValueStore generic interface. In any method that has an Integer overrideConsistency, that argument is ignored. In any method
 * that has an Integer overrideNumberOfRetries, that argument is ignored. TODO: This implementation should probably not use the Timestamper, at least by
 * default. It is better that we have hbase region servers pick the timestamp/version themselves by default. That way we rely on fewer machines having
 * synchronized clocks: The hbase region servers only, instead of all hbase clients.
 *
 * @param <T> type of object that will be used for tenantId information
 * @param <R> type of object that will be used for the row key
 * @param <C> type of object that will be used for column key
 * @param <V> type of object that will be used for values
 * @author jonathan
 */
public class HBaseSetOfSortedMapsImpl<T, R, C, V> implements RowColumnValueStore<T, R, C, V, Exception> {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger(true);
    public static final byte[] NULL = new byte[]{};
    // TODO Timestamper is supplied by the client and uses client's local time. We should have hbase region servers
    // pick the timestamp/version themselves, at least by default.
    private final Timestamper timestamper;
    private final RowColumnValueStoreMarshaller<T, R, C, V> marshaller;
    private final HTablePool tablePool;
    private final byte[] table;
    private final byte[] family;
    private final RowColumnValueStoreCounters counters;
    private final ExecutorService marshalExecutor;

    /**
     * Create a new wrapper over an HBase table.
     *
     * @param tablePool cannot be null
     * @param tableName cannot be null
     * @param family cannot be null
     * @param marshaller cannot be null
     * @param timestamper can't be null
     * @param marshalExecutor
     * @throws IOException
     */
    public HBaseSetOfSortedMapsImpl(
        HTablePool tablePool,
        String tableName,
        String family,
        RowColumnValueStoreMarshaller<T, R, C, V> marshaller,
        Timestamper timestamper,
        ExecutorService marshalExecutor) throws IOException {

        if (timestamper == null) {
            throw new IllegalArgumentException("timestamper cannot be null");
        }
        this.timestamper = timestamper;
        this.counters = new RowColumnValueStoreCounters(tableName);

        this.marshaller = marshaller;
        this.family = family.getBytes("UTF-8");
        this.table = tableName.getBytes("UTF-8");
        this.tablePool = tablePool;

        this.marshalExecutor = marshalExecutor;
    }

    // Un-tested
    /**
     * @param rowKey
     * @param columnKeys
     * @param columnValues
     * @param timeToLiveInSeconds ignored. Hbase allows a TTL per column family, not per column.
     */
    @Override
    public void multiAdd(final T tenantId, final R rowKey, final C[] columnKeys, final V[] columnValues,
            final Integer timeToLiveInSeconds, Timestamper overrideTimestamper) throws Exception {

        HTableInterface t = tablePool.getTable(table);
        try {
            final long timestamp = (overrideTimestamper == null) ? timestamper.get() : overrideTimestamper.get();
            final byte[] rawRowKey = marshaller.toRowKeyBytes(tenantId, rowKey);

            List<Put> puts = new LinkedList<>();
            for (int i = 0; i < columnKeys.length; i++) {
                byte[] rawColumnKey = marshaller.toColumnKeyBytes(columnKeys[i]);
                byte[] rawColumnValue = marshaller.toValueBytes(columnValues[i]);
                Put put = new Put(rawRowKey, timestamp);
                put.add(family, rawColumnKey, timestamp, rawColumnValue);
                puts.add(put);
            }
            t.put(puts);
            t.flushCommits();
            counters.added(puts.size());
        } catch (RowColumnValueStoreMarshallerException | IOException ex) {
            LOG.error("Exception multiAdd to hbase. customer=" + tenantId + " key=" + rowKey + " columnNames="
                    + columnKeys + " columnValues=" + columnValues, ex);
            throw ex;
        } finally {
            try {
                t.close();
            } catch (IOException e) {
                LOG.error("Failed to close hbase table!", e);
            }
        }

    }

    @Override
    public void multiRowsMultiAdd(final T tenantId, List<RowColumValueTimestampAdd<R, C, V>> multiAdd) throws Exception {

        HTableInterface t = tablePool.getTable(table);
        try {
            List<Put> puts = new LinkedList<>();
            for (RowColumValueTimestampAdd<R, C, V> add : multiAdd) {
                final byte[] rawRowKey = marshaller.toRowKeyBytes(tenantId, add.getRowKey());
                byte[] rawColumnKey = marshaller.toColumnKeyBytes(add.getColumnKey());
                byte[] rawColumnValue = marshaller.toValueBytes(add.getColumnValue());
                long timestamp = timestamper.get();
                if (add.getOverrideTimestamper() != null) {
                    timestamp = add.getOverrideTimestamper().get();
                }

                Put put = new Put(rawRowKey, timestamp);
                put.add(family, rawColumnKey, timestamp, rawColumnValue);
                puts.add(put);
            }
            t.put(puts);
            t.flushCommits();
            counters.added(puts.size());
        } catch (RowColumnValueStoreMarshallerException | IOException ex) {
            LOG.error("Exception multiAdd to hbase. customer=" + tenantId + " multiAdd=" + multiAdd, ex);
            throw ex;
        } finally {
            try {
                t.close();
            } catch (IOException e) {
                LOG.error("Failed to close hbase table!", e);
            }
        }

    }

    @Override
    public void multiRowsMultiAdd(List<TenantRowColumValueTimestampAdd<T, R, C, V>> multiAdd) throws Exception {

        HTableInterface t = tablePool.getTable(table);
        try {
            List<Put> puts = new LinkedList<>();
            for (TenantRowColumValueTimestampAdd<T, R, C, V> add : multiAdd) {
                final byte[] rawRowKey = marshaller.toRowKeyBytes(add.getTenantId(), add.getRowKey());
                long timestamp = timestamper.get();
                if (add.getOverrideTimestamper() != null) {
                    timestamp = add.getOverrideTimestamper().get();
                }

                Put put = new Put(rawRowKey, timestamp);
                if (add.getColumnKey() != null) {
                    byte[] rawColumnKey = marshaller.toColumnKeyBytes(add.getColumnKey());
                    byte[] rawColumnValue = marshaller.toValueBytes(add.getColumnValue());
                    put.add(family, rawColumnKey, timestamp, rawColumnValue);
                }
                puts.add(put);
            }
            t.put(puts);
            t.flushCommits();
            counters.added(puts.size());
        } catch (RowColumnValueStoreMarshallerException | IOException ex) {
            LOG.error("Exception multiAdd to hbase. multiAdd=" + multiAdd, ex);
            throw ex;
        } finally {
            try {
                t.close();
            } catch (IOException e) {
                LOG.error("Failed to close hbase table!", e);
            }
        }

    }

    /**
     * @param rowKey
     * @param columnKey
     * @param columnValue
     * @param timeToLiveInSeconds ignored
     */
    @Override
    public void add(final T tenantId, final R rowKey, final C columnKey, final V columnValue,
            final Integer timeToLiveInSeconds, Timestamper overrideTimestamper) throws Exception {
        add(tenantId, rowKey, columnKey, columnValue, null, timeToLiveInSeconds, overrideTimestamper, false);
    }

    @Override
    public boolean addIfNotExists(final T tenantId, final R rowKey, final C columnKey, final V columnValue,
            final Integer timeToLiveInSeconds, final Timestamper overrideTimestamper) throws Exception {
        return add(tenantId, rowKey, columnKey, columnValue, null, timeToLiveInSeconds, overrideTimestamper, true);
    }

    @Override
    public boolean replaceIfEqualToExpected(T tenantId, R rowKey, C columnKey, V columnValue, V expectedValue,
            Integer timeToLiveInSeconds, Timestamper overrideTimestamper) throws Exception {
        return add(tenantId, rowKey, columnKey, columnValue, expectedValue, timeToLiveInSeconds, overrideTimestamper,
                true);
    }

    private boolean add(final T tenantId, final R rowKey, final C columnKey, final V columnValue, final V expectedValue,
            final Integer timeToLiveInSeconds, Timestamper overrideTimestamper, boolean checkValue) throws Exception {

        HTableInterface t = tablePool.getTable(table);
        try {
            final long timestamp = (overrideTimestamper == null) ? timestamper.get() : overrideTimestamper.get();
            final byte[] rawRowKey = marshaller.toRowKeyBytes(tenantId, rowKey);
            final byte[] rawColumnKey = marshaller.toColumnKeyBytes(columnKey);
            final byte[] rawColumnValue = marshaller.toValueBytes(columnValue);

            Put put = new Put(rawRowKey, timestamp);
            put.add(family, rawColumnKey, timestamp, rawColumnValue);

            boolean added;
            if (checkValue) {
                final byte[] rawExpectedValue = expectedValue != null ? marshaller.toValueBytes(expectedValue) : null;
                added = t.checkAndPut(rawRowKey, family, rawColumnKey, rawExpectedValue, put);
            } else {
                added = true;
                t.put(put);
            }
            t.flushCommits();
            if (added) {
                counters.added(1);
            }
            // if another thread's sending was slowed down, it removes that slow down by a notch
            return added;

        } catch (RowColumnValueStoreMarshallerException | IOException ex) {
            // slows down sending from another thread
            String columnValueAsString = columnValue.toString();
            if (columnValue instanceof byte[]) {
                columnValueAsString = toString((byte[]) columnValue, ".");
            }
            LOG.error("Exception writing to hbase. customer=" + tenantId + " key=" + rowKey + " columName=" + columnKey + " columnValue="
                    + columnValueAsString, ex);
            throw ex;
        } finally {
            try {
                t.close();
            } catch (IOException e) {
                LOG.error("Failed to close hbase table!", e);
            }
        }

    }

    String toString(byte[] strings, String delim) {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            string.append(String.valueOf(strings[i]));
            if (i < strings.length - 1) {
                string.append(delim);
            }
        }
        return string.toString();
    }

    /**
     * Gets multiple columns from a row.
     *
     * @param tenantId cannot be null
     * @param rowKey cannot be null
     * @param columnKeys
     * @param overrideNumberOfRetries
     * @param overrideConsistency
     * @return
     */
    @Override
    public List<V> multiGet(T tenantId, R rowKey, C[] columnKeys, Integer overrideNumberOfRetries, Integer overrideConsistency) throws Exception {
        List<V> got = new LinkedList<>();
        ColumnValueAndTimestamp<C, V, Long>[] entries = multiGetEntries(tenantId, rowKey, columnKeys, overrideNumberOfRetries, overrideConsistency);
        if (entries == null) {
            return null;
        }
        for (ColumnValueAndTimestamp<C, V, Long> entry : entries) {
            if (entry == null) {
                got.add(null);
            } else {
                got.add(entry.getValue());
            }
        }
        return got;
    }

    @Override
    public ColumnValueAndTimestamp<C, V, Long>[] multiGetEntries(T tenantId, R rowKey, C[] columnKeys, Integer overrideNumberOfRetries,
            Integer overrideConsistency) throws Exception {

        HTableInterface t = tablePool.getTable(table);
        try {

            byte[] rawRowKey = marshaller.toRowKeyBytes(tenantId, rowKey);
            ColumnValueAndTimestamp<C, V, Long>[] got = null;

            if (columnKeys != null) {
                Get get = new Get(rawRowKey);
                for (C columnKey : columnKeys) {
                    byte[] rawColumnKey = marshaller.toColumnKeyBytes(columnKey);
                    get.addColumn(family, rawColumnKey);
                }

                Result result = t.get(get);
                if (result != null) {
                    got = new ColumnValueAndTimestamp[columnKeys.length];
                    for (int i = 0; i < columnKeys.length; i++) {
                        C c = columnKeys[i];
                        byte[] rawColumnKey = marshaller.toColumnKeyBytes(c);
                        KeyValue mostRecent = result.getColumnLatest(family, rawColumnKey);
                        if (mostRecent != null) {
                            V v = marshaller.fromValueBytes(mostRecent.getValue());
                            Long timestamp = mostRecent.getTimestamp();
                            got[i] = new ColumnValueAndTimestamp<>(columnKeys[i], v, timestamp);
                        }
                    }
                }
            } else {
                Get get = new Get(rawRowKey);
                get.addFamily(family);

                Result result = t.get(get);
                if (result != null) {
                    got = new ColumnValueAndTimestamp[result.size()];
                    KeyValue[] raw = result.raw();
                    for (int i = 0; i < raw.length; i++) {
                        KeyValue mostRecent = raw[i];
                        C c = marshaller.fromColumnKeyBytes(mostRecent.getQualifier());
                        V v = marshaller.fromValueBytes(mostRecent.getValue());
                        Long timestamp = mostRecent.getTimestamp();
                        got[i] = new ColumnValueAndTimestamp<>(c, v, timestamp);
                    }
                }
            }

            counters.got(1);
            return got;
        } catch (RowColumnValueStoreMarshallerException | IOException ex) {
            LOG.error("Failed to retrieve keys. customer=" + tenantId + " key=" + rowKey + " columnName=" + columnKeys, ex);
            throw ex;
        } finally {
            try {
                t.close();
            } catch (IOException e) {
                LOG.error("Failed to close hbase table!", e);
            }
        }
    }

    /**
     * Gets a single column from a row.
     *
     * @param rowKey
     * @param columnKey
     * @param overRideConsistency ignored
     * @return
     * @throws Exception
     */
    @Override
    public V get(final T tenantId, final R rowKey, final C columnKey, Integer overrideNumberOfRetries, Integer overRideConsistency) throws Exception {

        HTableInterface t = tablePool.getTable(table);
        try {
            final byte[] rawRowKey = marshaller.toRowKeyBytes(tenantId, rowKey);
            final byte[] rawColumnKey = marshaller.toColumnKeyBytes(columnKey);

            Get get = new Get(rawRowKey);
            get.addColumn(family, rawColumnKey);
            Result result = t.get(get);
            if (result.isEmpty()) {
                return null;
            }
            V v = marshaller.fromValueBytes(result.value());
            counters.got(1);
            return v;
        } catch (RowColumnValueStoreMarshallerException | IOException ex) {
            LOG.error("Failed to retrieve key. customer=" + tenantId + " key=" + rowKey + " columnName=" + columnKey, ex);
            throw ex;
        } finally {
            try {
                t.close();
            } catch (IOException e) {
                LOG.error("Failed to close hbase table!", e);
            }
        }
    }

    @Override
    public void multiRemove(final T tenantId, final R rowKey, final C[] columnKeys, Timestamper overrideTimestamper) throws Exception {

        HTableInterface t = tablePool.getTable(table);
        try {
            final long timestamp = (overrideTimestamper == null) ? timestamper.get() : overrideTimestamper.get();
            final byte[] rawRowKey = marshaller.toRowKeyBytes(tenantId, rowKey);

            List<Delete> deletes = new LinkedList<>();
            for (int i = 0; i < columnKeys.length; i++) {
                byte[] rawColumnKey = marshaller.toColumnKeyBytes(columnKeys[i]);
                Delete delete = new Delete(rawRowKey, timestamp, null);
                delete.deleteColumns(family, rawColumnKey, timestamp);
                deletes.add(delete);
            }
            t.delete(deletes);
            t.flushCommits();
            counters.removed(deletes.size());

        } catch (RowColumnValueStoreMarshallerException | IOException ex) {
            LOG.error("Exception multiAdd to hbase. customer=" + tenantId + " key=" + rowKey + " columnNames=" + columnKeys, ex);
            throw ex;
        } finally {
            try {
                t.close();
            } catch (IOException e) {
                LOG.error("Failed to close hbase table!", e);
            }
        }
    }

    @Override
    public void multiRowsMultiRemove(final T tenantId, List<RowColumnTimestampRemove<R, C>> multiRemove) throws Exception {

        HTableInterface t = tablePool.getTable(table);
        try {

            List<Delete> deletes = new LinkedList<>();
            for (RowColumnTimestampRemove<R, C> remove : multiRemove) {
                byte[] rawRowKey = marshaller.toRowKeyBytes(tenantId, remove.getRowKey());
                byte[] rawColumnKey = marshaller.toColumnKeyBytes(remove.getColumnKey());
                long timestamp = timestamper.get();
                if (remove.getOverrideTimestamper() != null) {
                    timestamp = remove.getOverrideTimestamper().get();
                }
                Delete delete = new Delete(rawRowKey, timestamp, null);
                delete.deleteColumns(family, rawColumnKey, timestamp);
                deletes.add(delete);
            }
            t.delete(deletes);
            t.flushCommits();
            counters.removed(deletes.size());

        } catch (RowColumnValueStoreMarshallerException | IOException ex) {
            LOG.error("Exception multiAdd to hbase. customer=" + tenantId + " multiRemove=" + multiRemove, ex);
            throw ex;
        } finally {
            try {
                t.close();
            } catch (IOException e) {
                LOG.error("Failed to close hbase table!", e);
            }
        }
    }

    @Override
    public void multiRowsMultiRemove(List<TenantRowColumnTimestampRemove<T, R, C>> multiRemove) throws Exception {

        HTableInterface t = tablePool.getTable(table);
        try {

            List<Delete> deletes = new LinkedList<>();
            for (TenantRowColumnTimestampRemove<T, R, C> remove : multiRemove) {
                byte[] rawRowKey = marshaller.toRowKeyBytes(remove.getTenantId(), remove.getRowKey());
                byte[] rawColumnKey = marshaller.toColumnKeyBytes(remove.getColumnKey());
                long timestamp = timestamper.get();
                if (remove.getOverrideTimestamper() != null) {
                    timestamp = remove.getOverrideTimestamper().get();
                }
                Delete delete = new Delete(rawRowKey, timestamp, null);
                delete.deleteColumns(family, rawColumnKey, timestamp);
                deletes.add(delete);
            }
            t.delete(deletes);
            t.flushCommits();
            counters.removed(deletes.size());

        } catch (RowColumnValueStoreMarshallerException | IOException ex) {
            LOG.error("Exception multiAdd to hbase. multiRemove=" + multiRemove, ex);
            throw ex;
        } finally {
            try {
                t.close();
            } catch (IOException e) {
                LOG.error("Failed to close hbase table!", e);
            }
        }
    }

    /**
     * @param rowKey
     * @param columnKey
     * @throws Exception
     */
    @Override
    public void remove(final T tenantId, final R rowKey, final C columnKey, Timestamper overrideTimestamper) throws Exception {
        if (columnKey == null) {
            return;
        }

        HTableInterface t = tablePool.getTable(table);
        try {
            final long timestamp = (overrideTimestamper == null) ? timestamper.get() : overrideTimestamper.get();
            final byte[] rawRowKey = marshaller.toRowKeyBytes(tenantId, rowKey);
            final byte[] rawColumnKey = marshaller.toColumnKeyBytes(columnKey);

            Delete delete = new Delete(rawRowKey, timestamp, null);
            delete.deleteColumns(family, rawColumnKey, timestamp);

            t.delete(delete);
            t.flushCommits();
            counters.removed(1);

        } catch (RowColumnValueStoreMarshallerException | IOException ex) {
            LOG.error("Failed to remove. customer=" + tenantId + " key=" + rowKey + " columnName=" + columnKey, ex);
            throw ex;
        } finally {
            try {
                t.close();
            } catch (IOException e) {
                LOG.error("Failed to close hbase table!", e);
            }
        }

    }

    /**
     * @param tenantId
     * @param rowKey
     * @param startColumnKey
     * @param maxCount if null read entire column
     * @param batchSize
     * @param reversed
     * @param overrideNumberOfRetries ignored
     * @param overrideConsistency ignored
     * @param callback
     */
    @Override
    public void getKeys(final T tenantId, R rowKey, C startColumnKey, Long maxCount, int batchSize, boolean reversed, Integer overrideNumberOfRetries,
            Integer overrideConsistency, CallbackStream<C> callback) throws Exception {
        get(tenantId, rowKey, startColumnKey, maxCount, batchSize, reversed, overrideNumberOfRetries, overrideConsistency, callback,
                new ValueStoreMarshaller<KeyValue, C>() {
                    @Override
                    public C marshall(KeyValue keyValue) throws Exception {
                        return marshaller.fromColumnKeyBytes(keyValue.getQualifier());
                    }
                });
    }

    /**
     * @param tenantId
     * @param rowKey
     * @param startColumnKey
     * @param maxCount if null read entire column
     * @param batchSize
     * @param reversed
     * @param overrideNumberOfRetries ignored
     * @param overrideConsistency ignored
     * @param callback
     */
    @Override
    public void getValues(final T tenantId, R rowKey, C startColumnKey, Long maxCount, int batchSize, boolean reversed, Integer overrideNumberOfRetries,
            Integer overrideConsistency, CallbackStream<V> callback) throws Exception {
        get(tenantId, rowKey, startColumnKey, maxCount, batchSize, reversed, overrideNumberOfRetries, overrideConsistency, callback,
                new ValueStoreMarshaller<KeyValue, V>() {
                    @Override
                    public V marshall(KeyValue keyValue) throws Exception {
                        return marshaller.fromValueBytes(keyValue.getValue());
                    }
                });
    }

    /**
     * @param <TS>
     * @param tenantId
     * @param rowKey
     * @param startColumnKey
     * @param maxCount if null read entire column
     * @param batchSize
     * @param reversed
     * @param overrideNumberOfRetries ignored
     * @param overrideConsistency ignored
     * @param callback
     */
    @Override
    public <TS> void getEntrys(final T tenantId, R rowKey, C startColumnKey, Long maxCount, int batchSize, boolean reversed,
            Integer overrideNumberOfRetries, Integer overrideConsistency, CallbackStream<ColumnValueAndTimestamp<C, V, TS>> callback) throws Exception {

        get(tenantId, rowKey, startColumnKey, maxCount, batchSize, reversed, overrideNumberOfRetries, overrideConsistency, callback,
                new ValueStoreMarshaller<KeyValue, ColumnValueAndTimestamp<C, V, TS>>() {
                    @Override
                    public ColumnValueAndTimestamp<C, V, TS> marshall(KeyValue keyValue) throws Exception {
                        Object t = keyValue.getTimestamp();
                        return new ColumnValueAndTimestamp<>(marshaller.fromColumnKeyBytes(keyValue.getQualifier()), marshaller.fromValueBytes(keyValue
                                        .getValue()), (TS) t);
                    }
                });
    }

    /**
     * @param tenantId
     * @param rowKey
     * @param startColumnKey
     * @param maxCount
     * @param batchSize
     * @param reversed
     * @param overrideNumberOfRetries ignored
     * @param overrideConsistency ignored
     * @param callback
     * @param marshall
     * @param <K>
     */
    private <K> void get(final T tenantId, final R rowKey, final C startColumnKey, final Long maxCount, int batchSize,
            final boolean reversed, Integer overrideNumberOfRetries, Integer overrideConsistency,
            final CallbackStream<K> callback, final ValueStoreMarshaller<KeyValue, K> marshall) throws Exception {

        if (reversed) {
            throw new RuntimeException("Not supported by hbase");
        }

        final MutableLong gotCount = new MutableLong();
        final int desiredBatchSize = (maxCount == null) ? batchSize : (int) Math.min(maxCount, batchSize);
        final int marshalBatchSize = 24; //TODO expose to config
        HTableInterface t = tablePool.getTable(table);
        try {
            final byte[] rawRowKey = marshaller.toRowKeyBytes(tenantId, rowKey);
            final byte[] startColumnKeyBytes = (startColumnKey == null) ? null : marshaller.toColumnKeyBytes(startColumnKey);

            Get get = new Get(rawRowKey);
            get.addFamily(family);
            get.setMaxVersions(1);
            Scan scan = new Scan(get);
            scan.setBatch(desiredBatchSize);
            if (startColumnKeyBytes != null) {
                Filter columnRangeFilter = new ColumnRangeFilter(startColumnKeyBytes, true, null, true);
                scan.setFilter(columnRangeFilter);
            }
            ResultScanner resultScanner = t.getScanner(scan);
            List<Future<K>> marshalFutures = Lists.newArrayListWithCapacity(marshalBatchSize);
            EOS:
            for (Result result : resultScanner) {

                if (result.isEmpty()) {
                    continue;
                }
                counters.sliced(1);

                for (final KeyValue keyValue : result.list()) {
                    marshalFutures.add(marshalExecutor.submit(new Callable<K>() {
                        @Override
                        public K call() throws Exception {
                            return marshall.marshall(keyValue);
                        }
                    }));

                    if (marshalFutures.size() == marshalBatchSize) {
                        if (completeFutureCallbacks(maxCount, callback, gotCount, marshalFutures)) {
                            marshalFutures.clear();
                            break EOS;
                        } else {
                            marshalFutures.clear();
                        }
                    }
                }
            }
            if (!marshalFutures.isEmpty()) {
                completeFutureCallbacks(maxCount, callback, gotCount, marshalFutures);
            }
            // EOS end of stream
            try {
                callback.callback(null);
            } catch (Exception ex) {
                throw new CallbackStreamException(ex);
            }

        } catch (Exception ex) {
            LOG.error("Failed to get slice. customer=" + tenantId + " key=" + rowKey + " start=" + startColumnKey, ex);
            throw ex;
        } finally {
            try {
                t.close();
            } catch (IOException e) {
                LOG.error("Failed to close hbase table!", e);
            }
        }
    }

    private <K> boolean completeFutureCallbacks(Long maxCount, CallbackStream<K> callback, MutableLong gotCount, List<Future<K>> marshalFutures)
        throws InterruptedException, ExecutionException {

        for (Future<K> future : marshalFutures) {
            K marshalled = future.get();
            if (marshalled == null) {
                continue;
            }

            try {
                K returned = callback.callback(marshalled);
                if (marshalled != returned) {
                    return true;
                }
                gotCount.increment();
                if (maxCount != null) {
                    if (gotCount.longValue() >= maxCount) {
                        return true;
                    }
                }
            } catch (Exception ex) {
                throw new CallbackStreamException(ex);
            }
        }
        return false;
    }

    /**
     * @param batchSize
     * @param overrideNumberOfRetries ignored
     * @param callback
     */
    @Override
    public void getAllRowKeys(int batchSize, Integer overrideNumberOfRetries, CallbackStream<TenantIdAndRow<T, R>> callback) throws Exception {
        getRowKeys(null, null, null, batchSize, overrideNumberOfRetries, callback);
    }

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
    @Override
    public void getRowKeys(T tenantId, R startRowKey, R stopRowKey, int batchSize, Integer overrideNumberOfRetries,
            CallbackStream<TenantIdAndRow<T, R>> callback) throws Exception {
        HTableInterface t = tablePool.getTable(table);
        try {

            Scan scan = new Scan();
            if (tenantId != null) {
                if (startRowKey != null) {
                    scan.setStartRow(marshaller.toRowKeyBytes(tenantId, startRowKey));
                }
                if (stopRowKey != null) {
                    scan.setStopRow(marshaller.toRowKeyBytes(tenantId, stopRowKey));
                }
            }
            scan.setBatch(batchSize);
            ResultScanner resultScanner = t.getScanner(scan);
            EOS:
            for (Result result : resultScanner) {
                if (result.isEmpty()) {
                    continue;
                }
                for (KeyValue keyValue : result.list()) {
                    try {
                        byte[] rawRowKey = keyValue.getRow();
                        TenantIdAndRow<T, R> entry = marshaller.fromRowKeyBytes(rawRowKey);
                        try {
                            if (callback.callback(entry) != entry) {
                                // stop stream requested
                                break;
                            }
                        } catch (Exception ex) {
                            throw new CallbackStreamException(ex);
                        }
                    } catch (Exception x) {
                        LOG.error("unable to handle keySlice.", x);
                    }
                }
            }
            // EOS end of stream
            try {
                callback.callback(null);
            } catch (Exception ex) {
                throw new CallbackStreamException(ex);
            }

        } catch (Exception ex) {
            throw ex;
        } finally {
            try {
                t.close();
            } catch (IOException e) {
                LOG.error("Failed to close hbase table!", e);
            }
        }
    }

    @Override
    public void removeRow(final T tenantId, final R rowKey, final Timestamper overrideTimestamper) throws Exception {

        HTableInterface t = tablePool.getTable(table);
        try {
            final long timestamp = (overrideTimestamper == null) ? timestamper.get() : overrideTimestamper.get();
            final byte[] rawRowKey = marshaller.toRowKeyBytes(tenantId, rowKey);

            Delete delete = new Delete(rawRowKey, timestamp, null);
            delete.setTimestamp(timestamp);
            t.delete(delete);
            t.flushCommits();
            counters.removed(1);
        } catch (RowColumnValueStoreMarshallerException | IOException ex) {
            LOG.error("Failed to remove. customer=" + tenantId + " key=" + rowKey, ex);
            throw ex;
        } finally {
            try {
                t.close();
            } catch (IOException e) {
                LOG.error("Failed to close hbase table!", e);
            }
        }
    }

    /**
     * Returns a List containing the values for a specified column for multiple rows. If a row did not have a value for that column (or if the row does not
     * exist), a null entry is inserted in the list instead.
     *
     * @param tenantId
     * @param rowKeys
     * @param columnKey
     * @param overrideNumberOfRetries
     * @param overrideConsistency
     * @return
     */
    @Override
    public List<V> multiRowGet(T tenantId, List<R> rowKeys, C columnKey, Integer overrideNumberOfRetries, Integer overrideConsistency) throws Exception {
        List<Get> gets = new ArrayList<>(rowKeys.size());

        byte[] rawColumnKey = null;
        try {
            rawColumnKey = marshaller.toColumnKeyBytes(columnKey);

            for (R rowKey : rowKeys) {
                Get get = new Get(marshaller.toRowKeyBytes(tenantId, rowKey));
                get.addColumn(family, rawColumnKey);
                gets.add(get);
            }
        } catch (Exception e) {
            throw e;
        }

        HTableInterface t = tablePool.getTable(table);
        try {
            List<V> values = new ArrayList<>(rowKeys.size());

            Result[] results = t.get(gets);

            for (Result result : results) {
                if (!result.isEmpty()) {
                    values.add(marshaller.fromValueBytes(result.value()));
                } else {
                    values.add(null);
                }
                counters.got(1);
            }

            return values;
        } catch (IOException | RowColumnValueStoreMarshallerException ex) {
            LOG.error("Failed to retrieve key. customer=" + tenantId + " keys=" + rowKeys + " columnName=" + columnKey, ex);
            throw ex;
        } finally {
            try {
                t.close();
            } catch (IOException e) {
                LOG.error("Failed to close hbase table!", e);
            }
        }
    }

    /**
     * Returns a List containing the values for specified columns for multiple rows (as a map from column to its value). If a row does not exist, a null entry
     * is inserted in the list instead. If there's no value for a specific column and a specific row, there would be no entry in the corresponding map.
     *
     * @param tenantId
     * @param rowKeys
     * @param columnKeys
     * @param overrideNumberOfRetries
     * @param overrideConsistency
     * @return
     */
    @Override
    public List<Map<C, V>> multiRowMultiGet(T tenantId, List<R> rowKeys, List<C> columnKeys, Integer overrideNumberOfRetries, Integer overrideConsistency)
            throws Exception {
        List<Get> gets = new ArrayList<>(rowKeys.size());

        Map<ByteBuffer, C> rawColumnKeys;
        try {
            rawColumnKeys = new HashMap<>(columnKeys.size());
            for (C columnKey : columnKeys) {
                rawColumnKeys.put(ByteBuffer.wrap(marshaller.toColumnKeyBytes(columnKey)), columnKey);
            }
            for (R rowKey : rowKeys) {
                Get get = new Get(marshaller.toRowKeyBytes(tenantId, rowKey));
                for (ByteBuffer rawColumnKey : rawColumnKeys.keySet()) {
                    get.addColumn(family, rawColumnKey.array());
                }
                gets.add(get);
            }
        } catch (Exception e) {
            throw e;
        }

        HTableInterface t = tablePool.getTable(table);
        try {
            List<Map<C, V>> values = new ArrayList<>(rowKeys.size());

            Result[] results = t.get(gets);

            for (Result result : results) {
                if (!result.isEmpty()) {
                    Map<C, V> map = new HashMap<>(columnKeys.size());
                    values.add(map);
                    for (KeyValue keyValue : result.list()) {
                        byte[] rawColumnKey = keyValue.getQualifier();
                        map.put(rawColumnKeys.get(ByteBuffer.wrap(rawColumnKey)), marshaller.fromValueBytes(keyValue.getValue()));
                    }
                } else {
                    values.add(null);
                }
                counters.got(1);
            }

            return values;
        } catch (IOException | RowColumnValueStoreMarshallerException ex) {
            LOG.error("Failed to retrieve key. customer=" + tenantId + " keys=" + rowKeys + " columnNames=" + columnKeys, ex);
            throw ex;
        } finally {
            try {
                t.close();
            } catch (IOException e) {
                LOG.error("Failed to close hbase table!", e);
            }
        }
    }

    @Override
    public <TS> void multiRowGetAll(T tenantId, List<KeyedColumnValueCallbackStream<R, C, V, TS>> rowKeyCallbackStreamPairs) throws Exception {
        List<Get> gets = new ArrayList<>(rowKeyCallbackStreamPairs.size());
        try {
            for (KeyedColumnValueCallbackStream<R, C, V, TS> pair : rowKeyCallbackStreamPairs) {
                Get get = new Get(marshaller.toRowKeyBytes(tenantId, pair.getKey()));
                get.addFamily(family);
                get.setMaxVersions(1);
                gets.add(get);
            }

        } catch (RowColumnValueStoreMarshallerException | IOException ex) {
            throw ex;
        }

        HTableInterface t = tablePool.getTable(table);
        try {
            //if this eagerly pulls back result values, this is an OOM waiting to happen
            Result[] results = t.get(gets);

            for (int i = 0; i < results.length; i++) {
                Result result = results[i];
                CallbackStream<ColumnValueAndTimestamp<C, V, TS>> callbackStream = rowKeyCallbackStreamPairs.get(i).getCallbackStream();

                if (result == null || result.isEmpty()) {
                    //eos
                    try {
                        callbackStream.callback(null);
                    } catch (Exception ex) {
                        throw new CallbackStreamException(ex);
                    }
                } else {
                    for (KeyValue keyValue : result.list()) {
                        C fromColumnKeyBytes = marshaller.fromColumnKeyBytes(keyValue.getQualifier());
                        V fromValueBytes = marshaller.fromValueBytes(keyValue.getValue());

                        try {
                            ColumnValueAndTimestamp<C, V, TS> cvat = new ColumnValueAndTimestamp<>(
                                fromColumnKeyBytes, fromValueBytes,
                                (TS) (Object) keyValue.getTimestamp());
                            if (callbackStream.callback(cvat) != cvat) {
                                break;
                            }
                        } catch (Exception ex) {
                            throw new CallbackStreamException(ex);
                        }

                    }
                    try {
                        callbackStream.callback(null);
                    } catch (Exception ex) {
                        throw new CallbackStreamException(ex);
                    }
                    counters.got(1);
                }
            }

        } catch (Exception ex) {
            LOG.error("Failed to retrieve key. customer=" + tenantId + " keys=" + rowKeyCallbackStreamPairs + " (all columns)", ex);
            throw ex;
        } finally {
            try {
                t.close();
            } catch (IOException e) {
                LOG.error("Failed to close hbase table!", e);
            }
        }
    }

    @Override
    public <TS> void multiRowGetAll(List<TenantKeyedColumnValueCallbackStream<T, R, C, V, TS>> rowKeyCallbackStreamPairs) throws Exception {
        List<Get> gets = new ArrayList<>(rowKeyCallbackStreamPairs.size());
        try {
            for (TenantKeyedColumnValueCallbackStream<T, R, C, V, TS> pair : rowKeyCallbackStreamPairs) {
                Get get = new Get(marshaller.toRowKeyBytes(pair.getTenantId(), pair.getKey()));
                get.addFamily(family);
                get.setMaxVersions(1);
                gets.add(get);
            }

        } catch (RowColumnValueStoreMarshallerException | IOException ex) {
            throw ex;
        }

        HTableInterface t = tablePool.getTable(table);
        try {
            //if this eagerly pulls back result values, this is an OOM waiting to happen
            Result[] results = t.get(gets);

            for (int i = 0; i < results.length; i++) {
                Result result = results[i];
                CallbackStream<ColumnValueAndTimestamp<C, V, TS>> callbackStream = rowKeyCallbackStreamPairs.get(i).getCallbackStream();

                if (result == null || result.isEmpty()) {
                    //eos
                    try {
                        callbackStream.callback(null);
                    } catch (Exception ex) {
                        throw new CallbackStreamException(ex);
                    }
                } else {
                    for (KeyValue keyValue : result.list()) {
                        C fromColumnKeyBytes = marshaller.fromColumnKeyBytes(keyValue.getQualifier());
                        V fromValueBytes = marshaller.fromValueBytes(keyValue.getValue());

                        try {
                            ColumnValueAndTimestamp<C, V, TS> cvat = new ColumnValueAndTimestamp<>(
                                fromColumnKeyBytes, fromValueBytes,
                                (TS) (Object) keyValue.getTimestamp());
                            if (callbackStream.callback(cvat) != cvat) {
                                break;
                            }
                        } catch (Exception ex) {
                            throw new CallbackStreamException(ex);
                        }

                    }
                    try {
                        callbackStream.callback(null);
                    } catch (Exception ex) {
                        throw new CallbackStreamException(ex);
                    }
                    counters.got(1);
                }
            }

        } catch (Exception ex) {
            LOG.error("Failed to retrieve keys=" + rowKeyCallbackStreamPairs + " (all columns)", ex);
            throw ex;
        } finally {
            try {
                t.close();
            } catch (IOException e) {
                LOG.error("Failed to close hbase table!", e);
            }
        }
    }
}
