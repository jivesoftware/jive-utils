package com.jivesoftware.os.jive.utils.row.column.value.store.hbase.ha.tests;


import com.jivesoftware.os.jive.utils.row.column.value.store.api.DefaultRowColumnValueStoreMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.MasterSlaveHASetOfSortedMaps;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.MasterSlaveHASetOfSortedMaps.ReadFailureMode;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.CurrentTimestamper;
import com.jivesoftware.os.jive.utils.row.column.value.store.hbase.HBaseSetOfSortedMapsImplInitializer;
import com.jivesoftware.os.jive.utils.row.column.value.store.hbase.HBaseSetOfSortedMapsImplInitializer.HBaseSetOfSortedMapsConfig;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.primatives.LongTypeMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.primatives.StringTypeMarshaller;
import java.io.IOException;
import org.merlin.config.BindInterfaceToConfiguration;

public class Main {

    public static void main(String[] args) throws Exception {
        HBaseSetOfSortedMapsConfig masterConfig = BindInterfaceToConfiguration.bindDefault(HBaseSetOfSortedMapsConfig.class);
        masterConfig.setHBaseZookeeperQuorum(args[0]);
        HBaseSetOfSortedMapsConfig slaveConfig = BindInterfaceToConfiguration.bindDefault(HBaseSetOfSortedMapsConfig.class);
        slaveConfig.setHBaseZookeeperQuorum(args[1]);

        final MasterSlaveHASetOfSortedMaps<String, String, String, Long, Exception> store = new MasterSlaveHASetOfSortedMaps<>(
                getStore(masterConfig),
                ReadFailureMode.failToSlave,
                getStore(slaveConfig));

        final String tenantId = "ha-test";
        final String rowKey = "ha";
        final String columnKey = "test";

        final long sleep = 10;
        Thread reader = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Long timestamp = store.get(tenantId, rowKey, columnKey, null, null);
                        if (timestamp != null) {
                            long lag = System.currentTimeMillis() - timestamp;
                            if (lag >= 0) {
                                System.out.println("INFO: Lag:" + lag);
                            } else {
                                System.out.println("WARN: applied change has disappeared. Lag:" + lag);
                            }
                        } else {
                            System.out.println("INFO: No timestamp in store.");
                        }
                        Thread.sleep(sleep);
                    } catch (Exception x) {
                        System.out.println("ERROR: Reader barfed." + x);
                    }
                }
            }
        };

        Thread writer = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        store.add(tenantId, rowKey, columnKey, System.currentTimeMillis(), null, null);
                        Thread.sleep(sleep);
                    } catch (Exception x) {
                        System.out.println("ERROR: Writer barfed." + x);
                    }
                }
            }
        };

        writer.start();
        reader.start();

    }

    private static RowColumnValueStore<String, String, String, Long, Exception> getStore(HBaseSetOfSortedMapsConfig config) throws IOException {
        HBaseSetOfSortedMapsImplInitializer masterSetOfSortedMapsImplInitializer = new HBaseSetOfSortedMapsImplInitializer(config);
        return masterSetOfSortedMapsImplInitializer.initialize(
                "ha", "ha.test", "cf", new DefaultRowColumnValueStoreMarshaller<>(
                new StringTypeMarshaller(),
                new StringTypeMarshaller(),
                new StringTypeMarshaller(),
                new LongTypeMarshaller()), new CurrentTimestamper());
    }
}
