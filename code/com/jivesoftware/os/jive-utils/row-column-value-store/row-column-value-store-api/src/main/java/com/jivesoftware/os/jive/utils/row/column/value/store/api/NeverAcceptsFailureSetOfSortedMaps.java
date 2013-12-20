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

public class NeverAcceptsFailureSetOfSortedMaps<T, R, C, V> implements RowColumnValueStore<T, R, C, V, RuntimeException> {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();
    private final RowColumnValueStore<T, R, C, V, ?> store;
    private final ThunderingHerd thunderingHerd;

    public NeverAcceptsFailureSetOfSortedMaps(RowColumnValueStore<T, R, C, V, ?> store) {
        this.store = store;
        this.thunderingHerd = new ThunderingHerd();
    }

    @Override
    public void add(T tenantId, R rowKey, C columnKey, V columnValue, Integer timeToLiveInSeconds, Timestamper overrideTimestamper) {
        while (true) {
            try {
                thunderingHerd.herd();
                store.add(tenantId, rowKey, columnKey, columnValue, timeToLiveInSeconds, overrideTimestamper);
                return;
            } catch (Exception x) {
                thunderingHerd.pushback();
            } finally {
                thunderingHerd.progress();
            }
        }
    }

    @Override
    public boolean addIfNotExists(T tenantId, R rowKey, C columnKey, V columnValue, Integer timeToLiveInSeconds, Timestamper overrideTimestamper) {
        while (true) {
            try {
                thunderingHerd.herd();
                return store.addIfNotExists(tenantId, rowKey, columnKey, columnValue, timeToLiveInSeconds, overrideTimestamper);
            } catch (Exception x) {
                thunderingHerd.pushback();
            } finally {
                thunderingHerd.progress();
            }
        }
    }

    @Override
    public void multiAdd(T tenantId, R rowKey, C[] columnKeys, V[] columnValues, Integer timeToLiveInSeconds, Timestamper overrideTimestamper) {
        while (true) {
            try {
                thunderingHerd.herd();
                store.multiAdd(tenantId, rowKey, columnKeys, columnValues, timeToLiveInSeconds, overrideTimestamper);
                return;
            } catch (Exception x) {
                thunderingHerd.pushback();
            } finally {
                thunderingHerd.progress();
            }
        }
    }

    @Override
    public void multiRowsMultiAdd(T tenantId, List<RowColumValueTimestampAdd<R, C, V>> multiAdd) {
        while (true) {
            try {
                thunderingHerd.herd();
                store.multiRowsMultiAdd(tenantId, multiAdd);
                return;
            } catch (Exception x) {
                thunderingHerd.pushback();
            } finally {
                thunderingHerd.progress();
            }
        }
    }

    @Override
    public void multiRowsMultiAdd(List<TenantRowColumValueTimestampAdd<T, R, C, V>> multiAdd) {
        while (true) {
            try {
                thunderingHerd.herd();
                store.multiRowsMultiAdd(multiAdd);
                return;
            } catch (Exception x) {
                thunderingHerd.pushback();
            } finally {
                thunderingHerd.progress();
            }
        }
    }

    @Override
    public V get(T tenantId, R rowKey, C key, Integer overrideNumberOfRetries, Integer overrideConsistency) {
        while (true) {
            try {
                thunderingHerd.herd();
                return store.get(tenantId, rowKey, key, overrideNumberOfRetries, overrideConsistency);
            } catch (Exception x) {
                thunderingHerd.pushback();
            } finally {
                thunderingHerd.progress();
            }
        }
    }

    @Override
    public List<V> multiGet(T tenantId, R rowKey, C[] keys, Integer overrideNumberOfRetries, Integer overrideConsistency) {
        while (true) {
            try {
                thunderingHerd.herd();
                return store.multiGet(tenantId, rowKey, keys, overrideNumberOfRetries, overrideConsistency);
            } catch (Exception x) {
                thunderingHerd.pushback();
            } finally {
                thunderingHerd.progress();
            }
        }
    }

    @Override
    public ColumnValueAndTimestamp<C, V, Long>[] multiGetEntries(
            T tenantId, R rowKey, C[] columnKeys, Integer overrideNumberOfRetries, Integer overrideConsistency) {
        while (true) {
            try {
                thunderingHerd.herd();
                return store.multiGetEntries(tenantId, rowKey, columnKeys, overrideNumberOfRetries, overrideConsistency);
            } catch (Exception x) {
                thunderingHerd.pushback();
            } finally {
                thunderingHerd.progress();
            }
        }
    }

    @Override
    public void remove(T tenantId, R rowKey, C key, Timestamper overrideTimestamper) {
        while (true) {
            try {
                thunderingHerd.herd();
                store.remove(tenantId, rowKey, key, overrideTimestamper);
                return;
            } catch (Exception x) {
                thunderingHerd.pushback();
            } finally {
                thunderingHerd.progress();
            }
        }
    }

    @Override
    public void multiRemove(T tenantId, R rowKey, C[] key, Timestamper overrideTimestamper) {
        while (true) {
            try {
                thunderingHerd.herd();
                store.multiRemove(tenantId, rowKey, key, overrideTimestamper);
                return;
            } catch (Exception x) {
                thunderingHerd.pushback();
            } finally {
                thunderingHerd.progress();
            }
        }
    }

    @Override
    public void multiRowsMultiRemove(T tenantId, List<RowColumnTimestampRemove<R, C>> multiRemove) {
        while (true) {
            try {
                thunderingHerd.herd();
                store.multiRowsMultiRemove(tenantId, multiRemove);
                return;
            } catch (Exception x) {
                thunderingHerd.pushback();
            } finally {
                thunderingHerd.progress();
            }
        }
    }

    @Override
    public void multiRowsMultiRemove(List<TenantRowColumnTimestampRemove<T, R, C>> multiRemove) {
        while (true) {
            try {
                thunderingHerd.herd();
                store.multiRowsMultiRemove(multiRemove);
                return;
            } catch (Exception x) {
                thunderingHerd.pushback();
            } finally {
                thunderingHerd.progress();
            }
        }
    }

    @Override
    public void getKeys(T tenantId, R rowKey, Object start, Long maxCount, int batchSize,
            boolean reversed, Integer overrideNumberOfRetries, Integer overrideConsistency, CallbackStream<C> callback) {
        while (true) {
            try {
                thunderingHerd.herd();
                store.getKeys(tenantId, rowKey, start, maxCount, batchSize, reversed, overrideNumberOfRetries, overrideConsistency, callback);
                return;
            } catch (CallbackStreamException cex) {
                LOG.error("Caught exception invoking callback stream", cex);
            } catch (Exception x) {
                thunderingHerd.pushback();
            } finally {
                thunderingHerd.progress();
            }
        }
    }

    @Override
    public void getValues(T tenantId, R rowKey, Object start, Long maxCount, int batchSize,
            boolean reversed, Integer overrideNumberOfRetries, Integer overrideConsistency, CallbackStream<V> callback) {
        while (true) {
            try {
                thunderingHerd.herd();
                store.getValues(tenantId, rowKey, start, maxCount, batchSize, reversed, overrideNumberOfRetries, overrideConsistency, callback);
                return;
            } catch (CallbackStreamException cex) {
                LOG.error("Caught exception invoking callback stream", cex);
            } catch (Exception x) {
                thunderingHerd.pushback();
            } finally {
                thunderingHerd.progress();
            }
        }
    }

    @Override
    public <TS> void getEntrys(T tenantId, R rowKey, Object start, Long maxCount, int batchSize,
            boolean reversed, Integer overrideNumberOfRetries, Integer overrideConsistency, CallbackStream<ColumnValueAndTimestamp<C, V, TS>> callback) {
        while (true) {
            try {
                thunderingHerd.herd();
                store.getEntrys(tenantId, rowKey, start, maxCount, batchSize, reversed, overrideNumberOfRetries, overrideConsistency, callback);
                return;
            } catch (CallbackStreamException cex) {
                LOG.error("Caught exception invoking callback stream", cex);
            } catch (Exception x) {
                thunderingHerd.pushback();
            } finally {
                thunderingHerd.progress();
            }
        }
    }

    @Override
    public void getAllRowKeys(int batchSize, Integer overrideNumberOfRetries, CallbackStream<TenantIdAndRow<T, R>> callback) {
        while (true) {
            try {
                thunderingHerd.herd();
                store.getAllRowKeys(batchSize, overrideNumberOfRetries, callback);
                return;
            } catch (CallbackStreamException cex) {
                LOG.error("Caught exception invoking callback stream", cex);
            } catch (Exception x) {
                thunderingHerd.pushback();
            } finally {
                thunderingHerd.progress();
            }
        }
    }

    @Override
    public void removeRow(T tenantId, R rowKey, Timestamper overrideTimestamper) {
        while (true) {
            try {
                thunderingHerd.herd();
                store.removeRow(tenantId, rowKey, overrideTimestamper);
                return;
            } catch (Exception x) {
                thunderingHerd.pushback();
            } finally {
                thunderingHerd.progress();
            }
        }
    }

    @Override
    public List<V> multiRowGet(T tenantId, List<R> rowKeys, C columnKey, Integer overrideNumberOfRetries, Integer overrideConsistency) {
        while (true) {
            try {
                thunderingHerd.herd();
                return store.multiRowGet(tenantId, rowKeys, columnKey, overrideNumberOfRetries, overrideConsistency);
            } catch (Exception x) {
                thunderingHerd.pushback();
            } finally {
                thunderingHerd.progress();
            }
        }
    }

    @Override
    public List<Map<C, V>> multiRowMultiGet(T tenantId, List<R> rowKeys, List<C> columnKeys, Integer overrideNumberOfRetries, Integer overrideConsistency)
        throws RuntimeException {
        while (true) {
            try {
                thunderingHerd.herd();
                return store.multiRowMultiGet(tenantId, rowKeys, columnKeys, overrideNumberOfRetries, overrideConsistency);
            } catch (Exception x) {
                thunderingHerd.pushback();
            } finally {
                thunderingHerd.progress();
            }
        }
    }

    @Override
    public <TS> void multiRowGetAll(T tenantId, List<KeyedColumnValueCallbackStream<R, C, V, TS>> rowKeyCallbackStreamPairs) {
        while (true) {
            try {
                thunderingHerd.herd();
                store.multiRowGetAll(tenantId, rowKeyCallbackStreamPairs);
                return;
            } catch (CallbackStreamException cex) {
                LOG.error("Caught exception invoking callback stream", cex);
            } catch (Exception x) {
                thunderingHerd.pushback();
            } finally {
                thunderingHerd.progress();
            }
        }
    }

    @Override
    public <TS> void multiRowGetAll(List<TenantKeyedColumnValueCallbackStream<T, R, C, V, TS>> tenantRowKeyCallbackStreamPairs) {
        while (true) {
            try {
                thunderingHerd.herd();
                store.multiRowGetAll(tenantRowKeyCallbackStreamPairs);
                return;
            } catch (CallbackStreamException cex) {
                LOG.error("Caught exception invoking callback stream", cex);
            } catch (Exception x) {
                thunderingHerd.pushback();
            } finally {
                thunderingHerd.progress();
            }
        }
    }
}
