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
package com.jivesoftware.os.jive.utils.row.column.value.store.tests;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HBaseTestingUtility;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.mapred.MiniMRCluster;

/**
 * Embedded HBase cluster, using local ZooKeeper and DFS.  Optionally starts a MapReduce cluster as well.
 */
public class EmbeddedHBase {

    private HBaseTestingUtility hBaseTestingUtility;
    private MiniMRCluster mrCluster;

    public void start(boolean startMRCluster) throws Exception {
        System.setProperty("java.security.krb5.realm", "");
        System.setProperty("java.security.krb5.kdc", "");

        System.out.println("Starting HBase");
        Configuration configuration = HBaseConfiguration.create();
        // HBase won't automaticlly pick a master info port.
        configuration.set("hbase.master.info.port", String.valueOf(60000));
        // But HBase can automatically pick a region server info port.
        configuration.set(HConstants.REGIONSERVER_INFO_PORT_AUTO, "true");
        // All other ports are randomly selected.

        hBaseTestingUtility = new HBaseTestingUtility(configuration);

        hBaseTestingUtility.startMiniCluster();

        if (startMRCluster) {
            mrCluster = new MiniMRCluster(1, FileSystem.get(configuration).getUri().toString(), 1);
            configuration.set("mapred.job.tracker", "localhost:" + mrCluster.getJobTrackerPort());
        }
    }

    public Configuration getConfiguration() {
        return hBaseTestingUtility.getConfiguration();
    }

    public HTablePool getHTablePool() {
        return new HTablePool(hBaseTestingUtility.getConfiguration(), 5);
    }

    public HBaseAdmin getHBaseAdmin() throws MasterNotRunningException, ZooKeeperConnectionException, IOException {
        return new HBaseAdmin(getConfiguration());
    }

    public void stop() throws Exception {
        if (mrCluster != null) {
            mrCluster.shutdown();
            mrCluster = null;
        }

        if (hBaseTestingUtility != null) {
            hBaseTestingUtility.shutdownMiniCluster();
            hBaseTestingUtility = null;
        }
    }

    public FileSystem getFileSystem() throws IOException {
        return FileSystem.get(getConfiguration());
    }
}
