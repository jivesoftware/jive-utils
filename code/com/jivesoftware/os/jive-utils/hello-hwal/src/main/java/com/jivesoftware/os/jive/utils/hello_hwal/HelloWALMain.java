/*
 * Copyright 2014 Jive Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jivesoftware.os.jive.utils.hello_hwal;

import com.jivesoftware.os.jive.utils.hwal.read.WALCursorStore;
import com.jivesoftware.os.jive.utils.hwal.read.WALReaders;
import com.jivesoftware.os.jive.utils.hwal.read.WALReadersInitializer;
import com.jivesoftware.os.jive.utils.hwal.read.WALReadersInitializer.WALReadersConfig;
import com.jivesoftware.os.jive.utils.hwal.read.WALTopicReader;
import com.jivesoftware.os.jive.utils.hwal.read.WALTopicReader.WALTopicStream;
import com.jivesoftware.os.jive.utils.hwal.read.partitions.TopicLag;
import com.jivesoftware.os.jive.utils.hwal.read.rcvs.RCVSWALCursorStoreInitializer;
import com.jivesoftware.os.jive.utils.hwal.read.rcvs.RCVSWALCursorStoreInitializer.RCVSWALCursorStoreConfig;
import com.jivesoftware.os.jive.utils.hwal.read.rcvs.RCVSWALTopicReaderInitializer;
import com.jivesoftware.os.jive.utils.hwal.read.rcvs.RCVSWALTopicReaderInitializer.RCVSWALTopicReaderConfig;
import com.jivesoftware.os.jive.utils.hwal.read.topic.WALTopics;
import com.jivesoftware.os.jive.utils.hwal.read.topic.WALTopicsInitializer;
import com.jivesoftware.os.jive.utils.hwal.read.topic.WALTopicsInitializer.WALTopicsConfig;
import com.jivesoftware.os.jive.utils.hwal.shared.api.SipWALEntry;
import com.jivesoftware.os.jive.utils.hwal.shared.api.WALEntry;
import com.jivesoftware.os.jive.utils.hwal.shared.api.WALService;
import com.jivesoftware.os.jive.utils.hwal.shared.filter.IncludeAnyFilter;
import com.jivesoftware.os.jive.utils.hwal.shared.partition.RandomPartitioningStrategy;
import com.jivesoftware.os.jive.utils.hwal.shared.rcvs.RCVSWALStorage;
import com.jivesoftware.os.jive.utils.hwal.shared.rcvs.RCVSWALStorageInitializer;
import com.jivesoftware.os.jive.utils.hwal.shared.rcvs.RCVSWALStorageInitializer.RCVSWALStorageConfig;
import com.jivesoftware.os.jive.utils.hwal.write.WALWriter;
import com.jivesoftware.os.jive.utils.hwal.write.rcvs.RCVSWALWriterInitializer;
import com.jivesoftware.os.jive.utils.hwal.write.rcvs.RCVSWALWriterInitializer.RCVSWALWriterConfig;
import com.jivesoftware.os.jive.utils.ordered.id.ConstantWriterIdProvider;
import com.jivesoftware.os.jive.utils.ordered.id.OrderIdProviderImpl;
import com.jivesoftware.os.jive.utils.permit.ConstantPermitConfig;
import com.jivesoftware.os.jive.utils.permit.PermitProvider;
import com.jivesoftware.os.jive.utils.permit.PermitProviderImplInitializer;
import com.jivesoftware.os.jive.utils.permit.PermitProviderImplInitializer.PermitProviderConfig;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.SetOfSortedMapsImplInitializer;
import com.jivesoftware.os.jive.utils.row.column.value.store.hbase.HBaseSetOfSortedMapsImplInitializer;
import com.jivesoftware.os.jive.utils.row.column.value.store.hbase.HBaseSetOfSortedMapsImplInitializer.HBaseSetOfSortedMapsConfig;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import org.merlin.config.BindInterfaceToConfiguration;

/**
 *
 * @author jonathan
 */
public class HelloWALMain {

    static AtomicReference<WAL> currentWAL = new AtomicReference<>();

    public static void main(String[] args) throws Exception {

        AtomicReference<String> hbaseZookeeperQuorum = new AtomicReference<>("soa-prime-data2.phx1.jivehosted.com");
        AtomicReference<String> topic = new AtomicReference<>("topic1");
        AtomicReference<String> readerGroup = new AtomicReference<>("reader-group1");
        AtomicReference<String> cursorGroup = new AtomicReference<>("cursor-group");

        OrderIdProviderImpl id = new OrderIdProviderImpl(new ConstantWriterIdProvider(1));

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print(":>");
                String s = br.readLine();
                if (s.startsWith("exit")) {
                    System.exit(0);
                } else if (s.startsWith("help")) {
                    System.out.println("Commands:");
                    System.out.println("\thelp  -  (displays this message)");
                    System.out.println("\tlag  -  (displays cursor lags)");
                    System.out.println("\tstress <count> <batchSize> -  (generates activity)");
                    System.out.println("\techo <message>  -  (places message on the queue)");
                    System.out.println("\tstatus  -  (displays current topic info)");
                    System.out.println("\tset <fieldName> <value>  -  (sets field to specified value. use 'status' to see valid field names)");
                    System.out.println("\tconntect  -  (connect to a WAL based on current setting)");
                    System.out.println("\tdisconntect  -  (disconnect from current WAL)");
                    System.out.println("\tstart  -  (start consuming from current topic)");
                    System.out.println("\tstop  -  (stop consuming from current topic)");

                } else if (s.startsWith("status")) {
                    System.out.println("hbaseZookeeperQuorum=" + hbaseZookeeperQuorum.get());
                    System.out.println("topic=" + topic.get());
                    System.out.println("readerGroup=" + readerGroup.get());
                    System.out.println("cursorGroup=" + cursorGroup.get());

                    WAL wal = currentWAL.get();
                    if (wal == null) {
                        System.out.println("Currently not connected to any wall type 'help' for more info.");
                    } else {
                        System.out.println("connected to WAL=" + currentWAL.get());
                    }

                } else if (s.startsWith("set")) {
                    String[] parts = s.split(" ");
                    if (parts.length == 3) {
                        if (parts[1].equals("hbaseZookeeperQuorum")) {
                            hbaseZookeeperQuorum.set(parts[2]);
                            System.out.println("Set " + parts[1] + "='" + parts[2] + "'");
                        } else if (parts[1].equals("topic")) {
                            topic.set(parts[2]);
                            System.out.println("Set " + parts[1] + "='" + parts[2] + "'");
                        } else if (parts[1].equals("readerGroup")) {
                            readerGroup.set(parts[2]);
                            System.out.println("Set " + parts[1] + "='" + parts[2] + "'");
                        } else if (parts[1].equals("cursorGroup")) {
                            cursorGroup.set(parts[2]);
                            System.out.println("Set " + parts[1] + "='" + parts[2] + "'");
                        } else {
                            System.out.println("Failed to set because the field name:'" + parts[1] + "' was not recogonized.");
                        }
                    } else {
                        System.out.println("invalid args to set. Set expects the following form: <fieldName> <value>");
                    }

                } else if (s.startsWith("connect")) {
                    WAL wal = currentWAL.get();
                    if (wal != null) {
                        wal.stop();
                        wal.dispose();
                    }
                    wal = new WAL(hbaseZookeeperQuorum.get(), topic.get(), readerGroup.get(), cursorGroup.get());
                    currentWAL.set(wal);
                } else if (s.startsWith("disconnect")) {
                    WAL wal = currentWAL.get();
                    if (wal != null) {
                        wal.stop();
                        wal.dispose();
                    }
                    currentWAL.set(null);
                } else if (s.startsWith("start")) {
                    WAL wal = currentWAL.get();
                    if (wal == null) {
                        System.out.println("Currently not connected to any wall type 'help' for more info.");
                    } else {
                        wal.walTopicReaderService.start();
                    }
                } else if (s.startsWith("stop")) {
                    WAL wal = currentWAL.get();
                    if (wal == null) {
                        System.out.println("Currently not connected to any wall type 'help' for more info.");
                    } else {
                        wal.walTopicReaderService.stop();
                    }
                } else if (s.startsWith("lag")) {
                    WAL wal = currentWAL.get();
                    if (wal == null) {
                        System.out.println("Currently not connected to any wall type 'help' for more info.");
                    } else {
                        List<TopicLag> topicLags = wal.topicReader.getTopicLags();
                        for (TopicLag lag : topicLags) {
                            System.out.println(lag);
                        }
                    }

                } else if (s.startsWith("stress")) {
                    WAL wal = currentWAL.get();
                    if (wal == null) {
                        System.out.println("Currently not connected to any wall type 'help' for more info.");
                    } else {
                        int count = 10000;
                        int batch = 1;
                        String[] parts = s.split(" ");
                        if (parts.length > 1) {
                            count = Integer.parseInt(parts[1]);
                        }
                        if (parts.length > 2) {
                            batch = Integer.parseInt(parts[2]);
                        }
                        String stressRun = "Stress run of " + count + " started at " + System.currentTimeMillis();
                        System.out.println("Starting:" + stressRun);
                        List<WALEntry> entries = new ArrayList<>();
                        for (int i = 0; i < count; i++) {
                            SipWALEntry sipWALEntry = new SipWALEntry(id.nextId(), System.currentTimeMillis(), s.getBytes());
                            String message = ("Stress message:" + i);
                            WALEntry entry = new WALEntry(sipWALEntry, message.getBytes());
                            entries.add(entry);
                            if (entries.size() >= batch) {
                                long start = System.currentTimeMillis();
                                wal.walWriter.write(wal.topic, entries);
                                System.out.println("Sent: " + entries.size() + " message in elapse(" + (System.currentTimeMillis() - start) + ")");
                                entries = new ArrayList<>();
                            }
                        }
                        if (!entries.isEmpty()) {
                            long start = System.currentTimeMillis();
                            wal.walWriter.write(wal.topic, entries);
                            System.out.println("Sent: " + entries.size() + " message in elapse(" + (System.currentTimeMillis() - start) + ")");
                        }
                        System.out.println("Finished:" + stressRun);
                    }
                } else if (s.startsWith("echo")) {
                    WAL wal = currentWAL.get();
                    if (wal == null) {
                        System.out.println("Currently not connected to any wall type 'help' for more info.");
                    } else {
                        if (s.length() > 0) {
                            SipWALEntry sipWALEntry = new SipWALEntry(id.nextId(), System.currentTimeMillis(), s.getBytes());
                            WALEntry entry = new WALEntry(sipWALEntry, s.getBytes());
                            wal.walWriter.write(wal.topic, Arrays.asList(entry));
                        }
                    }
                }
            } catch (Throwable t) {
                System.out.println(t);
                t.printStackTrace();
            }
        }

    }

    static public class WAL {

        public final String hbaseZookeeperQuorum;
        public final String topic;
        public final String readerGroup;
        public final String cursorGroup;

        WALWriter walWriter;
        WALTopicReader topicReader;

        WALService<WALTopicReader> walTopicReaderService;
        WALService<RCVSWALStorage> storageService;
        WALService<WALWriter> walWriterService;
        WALService<WALReaders> readersService;
        WALService<WALCursorStore> walCursorStorageService;
        WALService<WALTopics> walTopcisService;

        boolean consuming = false;

        public WAL(String hbaseZookeeperQuorum, String topic, final String readerGroup, String cursorGroup) throws Exception {
            this.hbaseZookeeperQuorum = hbaseZookeeperQuorum;
            this.topic = topic;
            this.readerGroup = readerGroup;
            this.cursorGroup = cursorGroup;

            HBaseSetOfSortedMapsConfig hbaseConfig = BindInterfaceToConfiguration.bindDefault(HBaseSetOfSortedMapsConfig.class);
            hbaseConfig.setHBaseZookeeperQuorum(hbaseZookeeperQuorum);
            hbaseConfig.setMarshalThreadPoolSize(4);
            SetOfSortedMapsImplInitializer<Exception> hBaseSetOfSortedMapsImplInitializer = new HBaseSetOfSortedMapsImplInitializer(hbaseConfig);

            RCVSWALStorageConfig storageConfig = BindInterfaceToConfiguration.bindDefault(RCVSWALStorageConfig.class);
            storageService = new RCVSWALStorageInitializer().initialize(storageConfig, hBaseSetOfSortedMapsImplInitializer);
            storageService.start();
            RCVSWALStorage storage = storageService.getService();

            RCVSWALWriterConfig writerConfig = BindInterfaceToConfiguration.bindDefault(RCVSWALWriterConfig.class);
            writerConfig.setNumberOfPartitions(10);

            walWriterService = new RCVSWALWriterInitializer()
                    .initialize(writerConfig, storage, new RandomPartitioningStrategy(new Random(1234)));
            walWriterService.start();
            walWriter = walWriterService.getService();

            PermitProviderConfig readerPermitsConfig = BindInterfaceToConfiguration.bindDefault(PermitProviderConfig.class);
            PermitProvider readersPermitProvider = new PermitProviderImplInitializer()
                    .initPermitProvider(readerPermitsConfig, hBaseSetOfSortedMapsImplInitializer);

            WALReadersConfig readerConfig = BindInterfaceToConfiguration.bindDefault(WALReadersConfig.class);
            readerConfig.setReaderGroupId(readerGroup);
            readerConfig.setHeartbeatIntervalInMillis(500);
            readersService = new WALReadersInitializer().initialize(readerConfig, readersPermitProvider);
            readersService.start();
            WALReaders readers = readersService.getService();

            RCVSWALCursorStoreConfig walCursorStorageConfig = BindInterfaceToConfiguration.bindDefault(RCVSWALCursorStoreConfig.class);
            walCursorStorageService = new RCVSWALCursorStoreInitializer().initialize(walCursorStorageConfig, storage);
            walCursorStorageService.start();
            WALCursorStore walCursorStore = walCursorStorageService.getService();

            PermitProviderConfig cursorPermitsConfig = BindInterfaceToConfiguration.bindDefault(PermitProviderConfig.class);
            PermitProvider cursorPermitProvider = new PermitProviderImplInitializer()
                    .initPermitProvider(cursorPermitsConfig, hBaseSetOfSortedMapsImplInitializer);

            WALTopicsConfig walTopicsConfig = BindInterfaceToConfiguration.bindDefault(WALTopicsConfig.class);
            walTopcisService = new WALTopicsInitializer()
                    .initialize(walTopicsConfig, readers, cursorPermitProvider,
                            new ConstantPermitConfig(0, walTopicsConfig.getNumberOfPartitions(), 5000), walCursorStore);
            walTopcisService.start();
            WALTopics walTopics = walTopcisService.getService();

            WALTopicStream walTopicStream = new WALTopicStream() {

                @Override
                public void stream(String topic, int partition, List<WALEntry> entries) {
                    for (WALEntry entry : entries) {
                        System.out.println("Topic:" + topic + " partition:" + partition + " readerGroup:" + readerGroup + " =>"
                                + new String(entry.getPayload()) + " batch size = " + entries.size());
                    }
                }
            };

            RCVSWALTopicReaderConfig topicReaderConfig = BindInterfaceToConfiguration.bindDefault(RCVSWALTopicReaderConfig.class);
            topicReaderConfig.setTopicId(topic);
            topicReaderConfig.setCursorGroup(cursorGroup);
            topicReaderConfig.setBatchSize(100);
            walTopicReaderService = new RCVSWALTopicReaderInitializer()
                    .initialize(topicReaderConfig, storage, walTopics, new IncludeAnyFilter(), walTopicStream);
            topicReader = walTopicReaderService.getService();
        }

        @Override
        public String toString() {
            return "WAL{"
                    + "hbaseZookeeperQuorum=" + hbaseZookeeperQuorum
                    + ", topic=" + topic
                    + ", readerGroup=" + readerGroup
                    + ", cursorGroup=" + cursorGroup
                    + ", consuming=" + consuming
                    + '}';
        }



        void start() throws Exception {
            walTopicReaderService.start();
            consuming = true;
        }

        void stop() throws Exception {
            walTopicReaderService.stop();
            consuming = false;
        }

        void dispose() throws Exception {
            storageService.stop();
            walWriterService.stop();
            readersService.stop();
            walCursorStorageService.stop();
            walTopcisService.stop();
        }

    }
}
