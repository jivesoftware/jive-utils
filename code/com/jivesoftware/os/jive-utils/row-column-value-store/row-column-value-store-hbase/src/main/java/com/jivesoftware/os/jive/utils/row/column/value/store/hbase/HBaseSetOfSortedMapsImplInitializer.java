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

import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStoreMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.SetOfSortedMapsImplInitializer;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.Timestamper;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.hadoop.hbase.client.HTablePool;
import org.merlin.config.Config;
import org.merlin.config.defaults.IntDefault;
import org.merlin.config.defaults.StringDefault;

public class HBaseSetOfSortedMapsImplInitializer implements SetOfSortedMapsImplInitializer<Exception> {

    private static final int POOL_SIZE = Integer.MAX_VALUE;

    static public interface HBaseSetOfSortedMapsConfig extends Config {

        @StringDefault ("unspecifiedHBaseZookeeperQuorum")
        public String getHBaseZookeeperQuorum();
        public void setHBaseZookeeperQuorum(String hbaseZookeeperQuorum);

        @IntDefault (2181)
        public Integer getHBaseZookeeperPort();
        public void setHBaseZookeeperPort(Integer hbaseZookeeperPort);

        @IntDefault(24)
        int getMarshalThreadPoolSize();
        public void setMarshalThreadPoolSize(int marshalThreadPoolSize);
    }
    private final org.apache.hadoop.conf.Configuration hbaseConfig;
    private final ExecutorService marshalExecutor;

    public HBaseSetOfSortedMapsImplInitializer(HBaseSetOfSortedMapsConfig config) {
        this(config, defaultHbaseConfig(config));
        defaultHbaseConfig(config);
    }

    public HBaseSetOfSortedMapsImplInitializer(HBaseSetOfSortedMapsConfig config, org.apache.hadoop.conf.Configuration hbaseConfig) {
        this.hbaseConfig = hbaseConfig;
        this.marshalExecutor = Executors.newFixedThreadPool(config.getMarshalThreadPoolSize());
    }

    private static org.apache.hadoop.conf.Configuration defaultHbaseConfig(HBaseSetOfSortedMapsConfig config) {
        org.apache.hadoop.conf.Configuration hbaseConfig = new org.apache.hadoop.conf.Configuration();
        hbaseConfig.clear();
        hbaseConfig.set("hbase.zookeeper.quorum", config.getHBaseZookeeperQuorum());
        hbaseConfig.set("hbase.zookeeper.property.clientPort", String.valueOf(config.getHBaseZookeeperPort()));
        return hbaseConfig;
    }

    @Override
    public <T, R, C, V> RowColumnValueStore<T, R, C, V, Exception> initialize(
        String tableNameSpace, String tableName, String columnFamilyName,
        RowColumnValueStoreMarshaller<T, R, C, V> marshaller,
        Timestamper timestamper) throws IOException {
        HBaseTableConfiguration hBaseTableConfiguration = new HBaseTableConfiguration(
            hbaseConfig,
            tableNameSpace,
            tableName,
            columnFamilyName);

        hBaseTableConfiguration.ensureTableProvisioned(true);

        return new HBaseSetOfSortedMapsImpl<>(
            new HTablePool(hbaseConfig, POOL_SIZE),
            hBaseTableConfiguration.getFinalName(),
            hBaseTableConfiguration.getColumnFamilyName(),
            marshaller,
            timestamper, marshalExecutor);
    }

    @Override
    public <T, R, C, V> RowColumnValueStore<T, R, C, V, Exception> initialize(String tableNameSpace, String tableName, String columnFamilyName,
        String[] additionalColumnFamilies, RowColumnValueStoreMarshaller<T, R, C, V> marshaller, Timestamper timestamper) throws IOException {

        String[] columnFamilies = new String[additionalColumnFamilies.length + 1];
        columnFamilies[0] = columnFamilyName;
        for (int i = 0; i < additionalColumnFamilies.length; i++) {
            columnFamilies[i + 1] = additionalColumnFamilies[i];
        }

        HBaseTableConfiguration hBaseTableConfiguration = new HBaseTableConfiguration(
            hbaseConfig,
            tableNameSpace,
            tableName,
            columnFamilies);

        hBaseTableConfiguration.ensureTableProvisioned(true, true);

        return new HBaseSetOfSortedMapsImpl<>(
            new HTablePool(hbaseConfig, POOL_SIZE),
            hBaseTableConfiguration.getFinalName(),
            hBaseTableConfiguration.getColumnFamilyName(),
            marshaller,
            timestamper, marshalExecutor);
    }

    @Override
    public <T, R, C, V> RowColumnValueStore<T, R, C, V, Exception> initialize(
        String tableNameSpace, String tableName, String columnFamilyName,
        int ttlInSeconds, int minVersions, int maxVersions,
        RowColumnValueStoreMarshaller<T, R, C, V> marshaller,
        Timestamper timestamper) throws IOException {
        HBaseTableConfiguration hBaseTableConfiguration = new HBaseTableConfiguration(
            hbaseConfig,
            tableNameSpace, tableName, columnFamilyName,
            ttlInSeconds, minVersions, maxVersions);

        hBaseTableConfiguration.ensureTableProvisioned(true);

        return new HBaseSetOfSortedMapsImpl<>(
            new HTablePool(hbaseConfig, POOL_SIZE),
            hBaseTableConfiguration.getFinalName(),
            hBaseTableConfiguration.getColumnFamilyName(),
            marshaller,
            timestamper, marshalExecutor);
    }
}
