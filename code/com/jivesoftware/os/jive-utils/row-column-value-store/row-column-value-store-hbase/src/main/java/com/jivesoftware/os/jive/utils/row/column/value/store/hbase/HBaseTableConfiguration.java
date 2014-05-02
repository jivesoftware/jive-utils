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

import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableExistsException;
import org.apache.hadoop.hbase.client.HBaseAdmin;

/**
 * Provides the meta data information about an HBase table.
 */
public class HBaseTableConfiguration {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger(true);

    private static final String NAMESPACE_AND_NAME_DELIM = ".";

    private final Configuration configuration;
    private final String tableNameSpace;
    private final String tableName;
    private final String columnFamilyName;
    private final String[] columnFamilyNames;
    private final int timeToLiveInSeconds;
    private final int minVersions;
    private final int maxVersions;

    public HBaseTableConfiguration(Configuration configuration,
        String tableNameSpace,
        String tableName,
        String columnFamilyName) {
        this(configuration, tableNameSpace, tableName, new String[] { columnFamilyName });
    }

    public HBaseTableConfiguration(Configuration configuration,
        String tableNameSpace,
        String tableName,
        String[] columnFamilyNames) {
        this(configuration, tableNameSpace, tableName, columnFamilyNames, -1, -1, -1);
    }

    public HBaseTableConfiguration(Configuration configuration,
        String tableNameSpace,
        String tableName,
        String columnFamilyName,
        int ttlInSeconds,
        int minVersions,
        int maxVersions) {
        this(configuration, tableNameSpace, tableName, new String[] { columnFamilyName }, ttlInSeconds, minVersions, maxVersions);
    }

    public HBaseTableConfiguration(Configuration configuration,
        String tableNameSpace,
        String tableName,
        String[] columnFamilyNames,
        int ttlInSeconds,
        int minVersions,
        int maxVersions) {
        if (configuration == null) {
            this.configuration = HBaseConfiguration.create();
        } else {
            this.configuration = configuration;
        }
        this.tableNameSpace = tableNameSpace.trim();
        this.tableName = tableName;
        this.columnFamilyName = columnFamilyNames[0];
        this.columnFamilyNames = columnFamilyNames;
        this.timeToLiveInSeconds = ttlInSeconds;
        this.minVersions = minVersions;
        this.maxVersions = maxVersions;

    }

    public byte[] ensureTableProvisioned(boolean createTable) throws IOException {
        return ensureTableProvisioned(createTable, false);
    }

    public byte[] ensureTableProvisioned(boolean createTable, boolean createColumnFamilies) throws IOException {
        HBaseAdmin admin = new HBaseAdmin(configuration);

        HTableDescriptor tableDescriptor = null;
        String finalName = getFinalName();
        try {
            tableDescriptor = admin.getTableDescriptor(finalName.getBytes());
        } catch (org.apache.hadoop.hbase.TableNotFoundException tnfe) {
            if (!createTable) {
                throw tnfe;
            }
        }
        if (tableDescriptor == null) {
            tableDescriptor = new HTableDescriptor(finalName);
            for (String columnFamily : columnFamilyNames) {
                HColumnDescriptor hColumnDescriptor = buildHColumnDescriptor(columnFamily);
                tableDescriptor.addFamily(hColumnDescriptor);
            }
            try {
                admin.createTable(tableDescriptor);
                LOG.info("Created table: " + finalName + " with column family names: " + Arrays.toString(columnFamilyNames));
            } catch (TableExistsException tee) {
                LOG.info("Cannot create table since it already exists: " + finalName + " with column family names: "
                    + Arrays.toString(columnFamilyNames), tee);
            }
        } else {
            for (String columnFamily : columnFamilyNames) {
                HColumnDescriptor column = tableDescriptor.getFamily(columnFamily.getBytes());
                if (column == null) {
                    if (createColumnFamilies) {
                        try {
                            HColumnDescriptor hColumnDescriptor = buildHColumnDescriptor(columnFamily);
                            if (admin.isTableEnabled(finalName)) {
                                admin.disableTable(finalName);
                            }
                            admin.addColumn(finalName, hColumnDescriptor);
                        } finally {
                            admin.enableTable(finalName);
                        }
                    } else {
                        LOG.error("Table '" + finalName + "' exists, but expected column family name '" + columnFamilyName + "' does not");
                        throw new IOException("Table '" + finalName + "' exists, but expected column family name '" + columnFamilyName + "' does not");
                    }
                }
            }
        }

        return finalName.getBytes();
    }

    private HColumnDescriptor buildHColumnDescriptor(String columnFamily) {
        HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(columnFamily);
        if (timeToLiveInSeconds > -1) {
            hColumnDescriptor.setTimeToLive(timeToLiveInSeconds);
        }
        if (maxVersions > -1) {
            hColumnDescriptor.setMaxVersions(maxVersions);
        }
        if (minVersions > -1) {
            hColumnDescriptor.setMinVersions(minVersions);
        }
        return hColumnDescriptor;
    }

    public String getTableNameSpace() {
        return tableNameSpace;
    }

    public String getUnprefixedTableName() {
        return tableName;
    }

    /**
     * Provides the full name to use when referencing the table in HBase which is a combination of the namespace
     * (if it exists) and the table name.
     *
     * @return name of table as HBase sees it.
     */
    public String getFinalName() {
        if (tableNameSpace != null && tableNameSpace.length() > 0) {
            return tableNameSpace + NAMESPACE_AND_NAME_DELIM + tableName;
        }

        return tableName;
    }

    public String getColumnFamilyName() {
        return columnFamilyName;
    }
}
