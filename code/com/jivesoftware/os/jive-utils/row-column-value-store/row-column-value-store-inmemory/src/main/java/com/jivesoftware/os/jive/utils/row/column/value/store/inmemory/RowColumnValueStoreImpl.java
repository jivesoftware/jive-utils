/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.row.column.value.store.inmemory;

import com.jivesoftware.os.jive.utils.base.interfaces.CallbackStream;
import com.jivesoftware.os.jive.utils.base.util.locks.StripingLocksProvider;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.CallbackStreamException;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.ColumnValueAndTimestamp;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.KeyedColumnValueCallbackStream;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumValueTimestampAdd;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnTimestampRemove;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.TenantIdAndRow;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.TenantKeyedColumnValueCallbackStream;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.TenantRowColumValueTimestampAdd;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.TenantRowColumnTimestampRemove;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.ValueStoreMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.Timestamper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * In-memory implementation of RowColumnValueStore generic interface. There are no timestamps/versions involved. Only the value from the last Put for a given
 * (rowKey, columnKey) is kept. In any method that has a Timestamper argument, that argument is ignored. In any method that has an Integer overrideConsistency,
 * that argument is ignored.
 *
 * @param <T>
 * @param <S>
 * @param <K>
 * @param <V>
 * @author jonathan
 */
public class RowColumnValueStoreImpl<T, S, K, V> implements RowColumnValueStore<T, S, K, V, RuntimeException> {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();
    private final Map<T, NavigableMap<S, NavigableMap<K, Timestamped<V>>>> tenantIdStores = new ConcurrentSkipListMap<>();
    private final StripingLocksProvider<S> rowLocks = new StripingLocksProvider<>(128);

    /**
     *
     */
    public RowColumnValueStoreImpl() {
    }

    private NavigableMap<S, NavigableMap<K, Timestamped<V>>> getStore(T tenantId) {
        synchronized (tenantIdStores) {
            NavigableMap<S, NavigableMap<K, Timestamped<V>>> store = tenantIdStores.get(tenantId);
            if (store == null) {
                store = new ConcurrentSkipListMap<>();
                tenantIdStores.put(tenantId, store);
            }
            return store;
        }
    }

    @Override
    public void add(T tenantId, S rowKey, K columnKey, V columnValue, Integer timeToLiveInSeconds, Timestamper overrideTimestamper) {
        NavigableMap<S, NavigableMap<K, Timestamped<V>>> store = getStore(tenantId);
        synchronized (rowLocks.lock(rowKey)) {
            NavigableMap<K, Timestamped<V>> map = store.get(rowKey);
            if (map == null) {
                // todo should use sortedMap to more closely mimic cassandra impl
                map = new ConcurrentSkipListMap<>();
                store.put(rowKey, map);
            }
            Timestamped<V> got = map.get(columnKey);
            if (got == null) {
                got = new Timestamped<>();
                map.put(columnKey, got);
            }
            got.set(columnValue, overrideTimestamper == null ? System.currentTimeMillis() : overrideTimestamper.get());

        }
    }

    @Override
    public boolean addIfNotExists(T tenantId, S rowKey, K columnKey, V columnValue, Integer timeToLiveInSeconds, Timestamper overrideTimestamper) {
        synchronized (rowLocks.lock(rowKey)) {
            V currentVal = get(tenantId, rowKey, columnKey, null, null);
            if (currentVal == null) {
                add(tenantId, rowKey, columnKey, columnValue, timeToLiveInSeconds, overrideTimestamper);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean replaceIfEqualToExpected(T tenantId, S rowKey, K columnKey, V columnValue, V expectedValue, Integer timeToLiveInSeconds, Timestamper overrideTimestamper) throws RuntimeException {
        synchronized (rowLocks.lock(rowKey)) {
            V currentVal = get(tenantId, rowKey, columnKey, null, null);
            if (expectedValue != null && !expectedValue.equals(currentVal)) {
                    return false;
            }
            else if (expectedValue == null && currentVal != null) {
                return false;
            }

            add(tenantId, rowKey, columnKey, columnValue, timeToLiveInSeconds, overrideTimestamper);
            return true;
        }
    }

    @Override
    public V get(T tenantId, S rowKey, K columnKey, Integer overrideNumberOfRetries, Integer overrideConsistency) {
        NavigableMap<S, NavigableMap<K, Timestamped<V>>> store = getStore(tenantId);
        synchronized (rowLocks.lock(rowKey)) {
            Map<K, Timestamped<V>> map = store.get(rowKey);
            if (map == null) {
                return null;
            }
            Timestamped<V> got = map.get(columnKey);
            if (got != null) {
                return got.getValue();
            }
            return null;
        }
    }

    @Override
    public void remove(T tenantId, S rowKey, K columnKey, Timestamper overrideTimestamper) {
        NavigableMap<S, NavigableMap<K, Timestamped<V>>> store = getStore(tenantId);
        synchronized (rowLocks.lock(rowKey)) {
            NavigableMap<K, Timestamped<V>> map = store.get(rowKey);
            long timestamp = overrideTimestamper == null ? System.currentTimeMillis() : overrideTimestamper.get();
            if (map == null) {
                // todo should use sortedMap to more closely mimic cassandra impl
                map = new ConcurrentSkipListMap<>();
                store.put(rowKey, map);
            }
            Timestamped<V> remove = map.get(columnKey);
            if (remove == null) {
                remove = new Timestamped<>();
                map.put(columnKey, remove);
            }
            remove.tombstone(timestamp);
        }
    }

    private <R> void get(T tenantId, S rowKey, K startColumnKey, Long maxCount, int batchSize, boolean reversed, CallbackStream<R> callback,
            ValueStoreMarshaller<Map.Entry<K, Timestamped<V>>, R> marshall) {
        try {
            NavigableMap<S, NavigableMap<K, Timestamped<V>>> store = getStore(tenantId);

            Map<K, Timestamped<V>> copy = new ConcurrentSkipListMap<>();
            synchronized (rowLocks.lock(rowKey)) {
                Map<K, Timestamped<V>> map = store.get(rowKey);
                if (map != null) {
                    copy.putAll(map);
                }
            }

            long count = 0;
            for (Map.Entry<K, Timestamped<V>> e : copy.entrySet()) {
                count++;
                if (e.getValue().isTombstone()) {
                    continue;
                }

                R marshalled = marshall.marshall(e);

                try {
                    R g = callback.callback(marshalled);
                    if (g != marshalled) {
                        break;
                    }
                } catch (Exception ex) {
                    throw new CallbackStreamException(ex);
                }

                if (maxCount != null) {
                    if (count >= maxCount) {
                        break;
                    }
                }
            }
            //End of stream
            callback.callback(null);

        } catch (Exception x) {
            throw new RuntimeException("Failed to get.", x);
        }
    }

    @Override
    public void getKeys(T tenantId, S rowKey, K startColumnKey, Long maxCount, int batchSize, boolean reversed, Integer overRideNumberOfRetries,
            Integer overrideConsistency, CallbackStream<K> callback) {
        get(tenantId, rowKey, startColumnKey, maxCount, batchSize, reversed, callback, new ValueStoreMarshaller<Map.Entry<K, Timestamped<V>>, K>() {
            @Override
            public K marshall(Map.Entry<K, Timestamped<V>> raw) throws Exception {
                Map.Entry<K, Timestamped<V>> e = raw;
                return e.getKey();
            }
        });
    }

    @Override
    public void getValues(T tenantId, S rowKey, K startColumnKey, Long maxCount, int batchSize, boolean reversed, Integer overRideNumberOfRetries,
            Integer overrideConsistency, CallbackStream<V> callback) {
        get(tenantId, rowKey, startColumnKey, maxCount, batchSize, reversed, callback, new ValueStoreMarshaller<Map.Entry<K, Timestamped<V>>, V>() {
            @Override
            public V marshall(Map.Entry<K, Timestamped<V>> raw) throws Exception {
                Map.Entry<K, Timestamped<V>> e = raw;
                return e.getValue().getValue();
            }
        });
    }

    @Override
    public <TS> void getEntrys(T tenantId, S rowKey, K startColumnKey, Long maxCount, int batchSize, boolean reversed, Integer overRideNumberOfRetries,
            Integer overrideConsistency, CallbackStream<ColumnValueAndTimestamp<K, V, TS>> callback) {
        get(tenantId, rowKey, startColumnKey, maxCount, batchSize, reversed, callback,
                new ValueStoreMarshaller<Map.Entry<K, Timestamped<V>>, ColumnValueAndTimestamp<K, V, TS>>() {
                    @Override
                    public ColumnValueAndTimestamp<K, V, TS> marshall(Map.Entry<K, Timestamped<V>> raw) throws Exception {
                        Map.Entry<K, Timestamped<V>> e = raw;
                        Object t = e.getValue().getTimestamp();
                        return new ColumnValueAndTimestamp<>(e.getKey(), e.getValue().getValue(), (TS) t);
                    }
                });
    }

    @Override
    public void multiRowsMultiAdd(T tenantId, List<RowColumValueTimestampAdd<S, K, V>> multiAdd) {
        for (RowColumValueTimestampAdd<S, K, V> add : multiAdd) {
            add(tenantId, add.getRowKey(), add.getColumnKey(), add.getColumnValue(), null, add.getOverrideTimestamper());
        }
    }

    @Override
    public void multiRowsMultiAdd(List<TenantRowColumValueTimestampAdd<T, S, K, V>> multiAdd) {
        for (TenantRowColumValueTimestampAdd<T, S, K, V> add : multiAdd) {
            add(add.getTenantId(),
                    add.getRowKey(),
                    add.getColumnKey(),
                    add.getColumnValue(),
                    null,
                    add.getOverrideTimestamper());
        }
    }

    @Override
    public void multiAdd(T tenantId, S rowKey, K[] columnKeys, V[] columnValues, Integer timeToLiveInSeconds, Timestamper overrideTimestamper) {
        if (columnKeys.length != columnValues.length) {
            throw new RuntimeException("keys.length must equal values.length");
        }
        synchronized (rowLocks.lock(rowKey)) {
            for (int i = 0; i < columnKeys.length; i++) {
                add(tenantId, rowKey, columnKeys[i], columnValues[i], timeToLiveInSeconds, overrideTimestamper);
            }
        }
    }

    @Override
    public List<V> multiGet(T tenantId, S rowKey, K[] columnKeys, Integer overrideNumberOfRetries, Integer overrideConsistency) {
        List<V> got = new LinkedList<>();
        synchronized (rowLocks.lock(rowKey)) {
            for (K k : columnKeys) {
                got.add(get(tenantId, rowKey, k, overrideNumberOfRetries, overrideConsistency));
            }
        }
        return got;
    }

    /**
     *
     * @param tenantId
     * @param rowKey
     * @param columnKeys
     * @param overrideNumberOfRetries
     * @param overrideConsistency
     * @return array size is the same size as columnKeys and indexes are stable.
     */
    @Override
    public ColumnValueAndTimestamp<K, V, Long>[] multiGetEntries(T tenantId, S rowKey, K[] columnKeys, Integer overrideNumberOfRetries,
            Integer overrideConsistency) {

        NavigableMap<S, NavigableMap<K, Timestamped<V>>> store = getStore(tenantId);
        synchronized (rowLocks.lock(rowKey)) {
            Map<K, Timestamped<V>> map = store.get(rowKey);
            if (map == null) {
                return new ColumnValueAndTimestamp[columnKeys.length];
            }
            List<ColumnValueAndTimestamp<K, V, Long>> got = new LinkedList<>();
            for (K columnKey : columnKeys) {
                Timestamped<V> value = map.get(columnKey);
                if (value == null) {
                    got.add(null);
                } else {
                    if (value.isTombstone()) {
                        got.add(null);
                    } else {
                        got.add(new ColumnValueAndTimestamp<>(columnKey, value.getValue(), value.getTimestamp()));
                    }
                }
            }
            return got.toArray(new ColumnValueAndTimestamp[got.size()]);

        }

    }

    @Override
    public void multiRemove(T tenantId, S rowKey, K[] columnKeys, Timestamper overrideTimestamper) {
        synchronized (rowLocks.lock(rowKey)) {
            for (K k : columnKeys) {
                remove(tenantId, rowKey, k, overrideTimestamper);
            }
        }
    }

    @Override
    public void multiRowsMultiRemove(T tenantId, List<RowColumnTimestampRemove<S, K>> multiRemove) {
        for (RowColumnTimestampRemove<S, K> remove : multiRemove) {
            remove(tenantId, remove.getRowKey(), remove.getColumnKey(), remove.getOverrideTimestamper());
        }
    }

    @Override
    public void multiRowsMultiRemove(List<TenantRowColumnTimestampRemove<T, S, K>> multiRemove) {
        for (TenantRowColumnTimestampRemove<T, S, K> remove : multiRemove) {
            remove(remove.getTenantId(),
                    remove.getRowKey(),
                    remove.getColumnKey(),
                    remove.getOverrideTimestamper());
        }
    }

    @Override
    public void getAllRowKeys(int batchSize, Integer overrideNumberOfRetries, CallbackStream<TenantIdAndRow<T, S>> callback) {
        getRowKeys(null, null, null, batchSize, overrideNumberOfRetries, callback);
    }

    @Override
    public void getRowKeys(T tenantId, S startRowKey, S stopRowKey, int batchSize, Integer overrideNumberOfRetries, CallbackStream<TenantIdAndRow<T, S>> callback) {
        if (tenantId != null) {
            NavigableMap<S, NavigableMap<K, Timestamped<V>>> store = getStore(tenantId);
            getRowKeysForTenant(tenantId, store, startRowKey, stopRowKey, batchSize, overrideNumberOfRetries, callback);
        }
        else {
            for (Entry<T, NavigableMap<S, NavigableMap<K, Timestamped<V>>>> entry : tenantIdStores.entrySet()) {
                getRowKeysForTenant(entry.getKey(), entry.getValue(), startRowKey, stopRowKey, batchSize, overrideNumberOfRetries, callback);
            }
        }

        try {
            callback.callback(null);
        } catch (Exception ex) {
            LOG.error("calling callback EOS.", ex);
        }
    }

    private void getRowKeysForTenant(T tenantId, NavigableMap<S, NavigableMap<K, Timestamped<V>>> rows, S startRowKey, S stopRowKey, int batchSize, Integer overrideNumberOfRetries, CallbackStream<TenantIdAndRow<T, S>> callback) {
        NavigableMap<S, NavigableMap<K, Timestamped<V>>> rowsFromStart;
        rowsFromStart = startRowKey != null ? rows.tailMap(startRowKey, true) : rows;

        NavigableMap<S, NavigableMap<K, Timestamped<V>>> rowsInRange;
        rowsInRange = stopRowKey != null ? rowsFromStart.headMap(stopRowKey, false) : rowsFromStart;

        for (S row : rowsInRange.keySet()) {
            synchronized (rowLocks.lock(row)) {
                TenantIdAndRow<T, S> rowKey = new TenantIdAndRow<>(tenantId, row);
                TenantIdAndRow<T, S> returned = null;
                try {
                    returned = callback.callback(rowKey);
                } catch (Exception ex) {
                    LOG.error("calling callback.", ex);
                }
                if (returned != rowKey) {
                    break;
                }
            }
        }
    }

    @Override
    public void removeRow(T tenantId, S rowKey, Timestamper overrideTimestamper) {
        NavigableMap<S, NavigableMap<K, Timestamped<V>>> store = getStore(tenantId);
        synchronized (rowLocks.lock(rowKey)) {
            Map<K, Timestamped<V>> columns = store.get(rowKey);
            if (columns != null) {
                for (Entry<K, Timestamped<V>> c : columns.entrySet()) {
                    remove(tenantId, rowKey, c.getKey(), overrideTimestamper);
                }
                if (columns.isEmpty()) {
                    store.remove(rowKey);
                }
            }
        }
    }

    @Override
    public List<V> multiRowGet(T tenantId, List<S> rowKeys, K columnKey, Integer overrideNumberOfRetries, Integer overrideConsistency) {
        List<V> values = new ArrayList<>(rowKeys.size());
        for (S rowKey : rowKeys) {
            values.add(get(tenantId, rowKey, columnKey, overrideNumberOfRetries, overrideConsistency));
        }
        return values;
    }

    @Override
    public List<Map<K, V>> multiRowMultiGet(T tenantId, List<S> rowKeys, List<K> columnKeys, Integer overrideNumberOfRetries, Integer overrideConsistency) {
        List<Map<K, V>> result = new ArrayList<>(rowKeys.size());
        for (S rowKey : rowKeys) {
            List<V> values = multiGet(tenantId, rowKey, (K[]) columnKeys.toArray(), overrideNumberOfRetries, overrideConsistency);
            Map<K, V> valuesMap = new HashMap<>(columnKeys.size());
            result.add(valuesMap);
            for (int i = 0; i < columnKeys.size(); i++) {
                K key = columnKeys.get(i);
                valuesMap.put(key, values.get(i));
            }
        }
        return result;
    }

    @Override
    public <TS> void multiRowGetAll(T tenantId, List<KeyedColumnValueCallbackStream<S, K, V, TS>> rowKeyCallbackStreamPairs) {
        NavigableMap<S, NavigableMap<K, Timestamped<V>>> store = getStore(tenantId);
        for (KeyedColumnValueCallbackStream<S, K, V, TS> pair : rowKeyCallbackStreamPairs) {
            S rowKey = pair.getKey();

            Map<K, Timestamped<V>> copy = new ConcurrentSkipListMap<>();
            synchronized (rowLocks.lock(rowKey)) {
                Map<K, Timestamped<V>> map = store.get(pair.getKey());
                if (map != null) {
                    copy.putAll(map);
                }
            }

            try {
                CallbackStream<ColumnValueAndTimestamp<K, V, TS>> callbackStream = pair.getCallbackStream();

                for (Map.Entry<K, Timestamped<V>> columnAndTimestamped : copy.entrySet()) {
                    K column = columnAndTimestamped.getKey();
                    Timestamped<V> got = columnAndTimestamped.getValue();
                    if (got.isTombstone()) {
                        continue;
                    }

                    try {
                        callbackStream.callback(new ColumnValueAndTimestamp<>(column, got.getValue(), (TS) (Object) got.getTimestamp()));
                    } catch (Exception ex) {
                        throw new CallbackStreamException(ex);
                    }
                }

                //eos
                callbackStream.callback(null);

            } catch (Exception ex) {
                throw new RuntimeException(ex);

            }
        }
    }

    @Override
    public <TS> void multiRowGetAll(List<TenantKeyedColumnValueCallbackStream<T, S, K, V, TS>> rowKeyCallbackStreamPairs) {

        for (TenantKeyedColumnValueCallbackStream<T, S, K, V, TS> pair : rowKeyCallbackStreamPairs) {
            NavigableMap<S, NavigableMap<K, Timestamped<V>>> store = getStore(pair.getTenantId());
            S rowKey = pair.getKey();
            Map<K, Timestamped<V>> copy = new ConcurrentSkipListMap<>();
            synchronized (rowLocks.lock(rowKey)) {
                Map<K, Timestamped<V>> map = store.get(pair.getKey());
                if (map != null) {
                    copy.putAll(map);
                }
            }

            try {
                CallbackStream<ColumnValueAndTimestamp<K, V, TS>> callbackStream = pair.getCallbackStream();

                for (Map.Entry<K, Timestamped<V>> columnAndTimestamped : copy.entrySet()) {
                    K column = columnAndTimestamped.getKey();
                    Timestamped<V> got = columnAndTimestamped.getValue();
                    if (got.isTombstone()) {
                        continue;
                    }

                    try {
                        callbackStream.callback(new ColumnValueAndTimestamp<>(column, got.getValue(), (TS) (Object) got.getTimestamp()));
                    } catch (Exception ex) {
                        throw new CallbackStreamException(ex);
                    }
                }

                //eos
                callbackStream.callback(null);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

        }
    }
}
