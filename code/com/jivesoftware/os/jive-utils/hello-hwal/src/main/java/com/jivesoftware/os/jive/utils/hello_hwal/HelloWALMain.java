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
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.merlin.config.BindInterfaceToConfiguration;

/**
 *
 * @author jonathan
 */
public class HelloWALMain {

    public static void main(String[] args) throws Exception {

        String ownerId = UUID.randomUUID().toString();

        System.out.println("ownerId:"+ownerId);

        args = new String[3];
        args[0] = "soa-prime-data2.phx1.jivehosted.com";
        args[1] = "topic1";
        args[2] = "reader-group1";

        String hbaseZookeeperQuorum = args[0];
        String topic = args[1];
        final String readerGroup = args[2];

        HBaseSetOfSortedMapsConfig hbaseConfig = BindInterfaceToConfiguration.bindDefault(HBaseSetOfSortedMapsConfig.class);
        hbaseConfig.setHBaseZookeeperQuorum(hbaseZookeeperQuorum);
        SetOfSortedMapsImplInitializer hBaseSetOfSortedMapsImplInitializer = new HBaseSetOfSortedMapsImplInitializer(hbaseConfig);

        RCVSWALStorageConfig storageConfig = BindInterfaceToConfiguration.bindDefault(RCVSWALStorageConfig.class);
        WALService<RCVSWALStorage> storageService = new RCVSWALStorageInitializer().initialize(storageConfig, hBaseSetOfSortedMapsImplInitializer);
        storageService.start();
        RCVSWALStorage storage = storageService.getService();

        RCVSWALWriterConfig writerConfig = BindInterfaceToConfiguration.bindDefault(RCVSWALWriterConfig.class);
        writerConfig.setNumberOfPartitions(10);

        WALService<WALWriter> walWriterService = new RCVSWALWriterInitializer()
                .initialize(writerConfig, storage, new RandomPartitioningStrategy(new Random(1234)));
        walWriterService.start();
        WALWriter walWriter = walWriterService.getService();

        PermitProviderConfig readerPermitsConfig = BindInterfaceToConfiguration.bindDefault(PermitProviderConfig.class);
        PermitProvider readersPermitProvider = new PermitProviderImplInitializer().initPermitProvider(readerPermitsConfig, hBaseSetOfSortedMapsImplInitializer);

        WALReadersConfig readerConfig = BindInterfaceToConfiguration.bindDefault(WALReadersConfig.class);
        readerConfig.setReaderGroupId(readerGroup);
        readerConfig.setHeartbeatIntervalInMillis(500);
        WALService<WALReaders> readersService = new WALReadersInitializer().initialize(readerConfig, readersPermitProvider);
        readersService.start();
        WALReaders readers = readersService.getService();

        RCVSWALCursorStoreConfig walCursorStorageConfig = BindInterfaceToConfiguration.bindDefault(RCVSWALCursorStoreConfig.class);
        WALService<WALCursorStore> walCursorStorageService = new RCVSWALCursorStoreInitializer().initialize(walCursorStorageConfig, storage);
        walCursorStorageService.start();
        WALCursorStore walCursorStore = walCursorStorageService.getService();

        PermitProviderConfig cursorPermitsConfig = BindInterfaceToConfiguration.bindDefault(PermitProviderConfig.class);
        PermitProvider cursorPermitProvider = new PermitProviderImplInitializer().initPermitProvider(cursorPermitsConfig, hBaseSetOfSortedMapsImplInitializer);

        WALTopicsConfig walTopicsConfig = BindInterfaceToConfiguration.bindDefault(WALTopicsConfig.class);
        WALService<WALTopics> walTopcisService = new WALTopicsInitializer()
                .initialize(walTopicsConfig, readers, cursorPermitProvider, new ConstantPermitConfig(0, 10, 5000), walCursorStore);
        walTopcisService.start();
        WALTopics walTopics = walTopcisService.getService();

        WALTopicStream walTopicStream = new WALTopicStream() {

            @Override
            public void stream(String topic, int partition, List<WALEntry> entries) {
                for (WALEntry entry : entries) {
                    System.out.println("Topic:" + topic + " parition:" + partition + " readerGroup:" + readerGroup + " =>"
                            + new String(entry.getPayload()) + " batch size = " + entries.size());
                }
            }
        };

        RCVSWALTopicReaderConfig topicReaderConfig = BindInterfaceToConfiguration.bindDefault(RCVSWALTopicReaderConfig.class);
        topicReaderConfig.setTopicId(topic);
        WALService<WALTopicReader> walTopicReaderService = new RCVSWALTopicReaderInitializer()
                .initialize(topicReaderConfig, storage, walTopics, new IncludeAnyFilter(), walTopicStream);
        walTopicReaderService.start();

        OrderIdProviderImpl id = new OrderIdProviderImpl(new ConstantWriterIdProvider(1));

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print(":>");
            String s = br.readLine();
            if (s.startsWith("exit")) {
                System.exit(0);
            } else if (s.startsWith("lag")) {

            } else if (s.startsWith("stress")) {
                int count = 10000;
                String stressRun = "Stress run of " + count + " started at " + System.currentTimeMillis();
                System.out.println("Starting:" + stressRun);
                for (int i = 0; i < 10000; i++) {
                    SipWALEntry sipWALEntry = new SipWALEntry(id.nextId(), System.currentTimeMillis(), s.getBytes());
                    String message = ("Stress message:" + i);
                    long start = System.currentTimeMillis();
                    WALEntry entry = new WALEntry(sipWALEntry, message.getBytes());
                    walWriter.write(topic, Arrays.asList(entry));
                    System.out.println("Sent: " + message + " elapse(" + (System.currentTimeMillis() - start) + ")");
                }
                System.out.println("Finished:" + stressRun);
            } else if (s.startsWith("echo")) {
                if (s.length() > 0) {
                    SipWALEntry sipWALEntry = new SipWALEntry(id.nextId(), System.currentTimeMillis(), s.getBytes());
                    WALEntry entry = new WALEntry(sipWALEntry, s.getBytes());
                    walWriter.write(topic, Arrays.asList(entry));
                }
            }
        }

    }
}
