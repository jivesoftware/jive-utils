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
package com.jivesoftware.os.jive.utils.hwal.read;

import com.jivesoftware.os.jive.utils.hwal.read.rcvs.RCVSWALCursorStoreInitializer;
import com.jivesoftware.os.jive.utils.hwal.read.rcvs.RCVSWALReaderInitializer;
import com.jivesoftware.os.jive.utils.hwal.read.topic.WALTopics;
import com.jivesoftware.os.jive.utils.hwal.read.topic.WALTopicsInitializer;
import com.jivesoftware.os.jive.utils.hwal.shared.api.SipWALEntry;
import com.jivesoftware.os.jive.utils.hwal.shared.api.WALEntry;
import com.jivesoftware.os.jive.utils.hwal.shared.api.WALKey;
import com.jivesoftware.os.jive.utils.hwal.shared.api.WALPayload;
import com.jivesoftware.os.jive.utils.hwal.shared.api.WALService;
import com.jivesoftware.os.jive.utils.hwal.shared.filter.IncludeAnyFilter;
import com.jivesoftware.os.jive.utils.hwal.shared.partition.RandomParitioningStrategy;
import com.jivesoftware.os.jive.utils.hwal.shared.rcvs.RCVSWALStorage;
import com.jivesoftware.os.jive.utils.hwal.write.WALWriter;
import com.jivesoftware.os.jive.utils.hwal.write.rcvs.RCVSWALWriterInitializer;
import com.jivesoftware.os.jive.utils.permit.ConstantPermitConfig;
import com.jivesoftware.os.jive.utils.permit.PermitProvider;
import com.jivesoftware.os.jive.utils.permit.PermitProviderImplInitializer;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;
import com.jivesoftware.os.jive.utils.row.column.value.store.inmemory.InMemorySetOfSortedMapsImplInitializer;
import com.jivesoftware.os.jive.utils.row.column.value.store.inmemory.RowColumnValueStoreImpl;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.merlin.config.BindInterfaceToConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author jonathan
 */
public class HelloWALTest {

    RowColumnValueStore<String, Integer, Long, WALEntry, ? extends Exception> wal;
    RowColumnValueStore<String, Integer, Long, SipWALEntry, ? extends Exception> sipWAL;
    RowColumnValueStore<String, Integer, Long, Long, ? extends Exception> cursors;

    WALService<WALWriter> walWriterService;
    WALWriter walWriter;

    WALService<WALReaders> readersService;
    WALReaders readers;

    WALService<WALTopicReader> walTopicReaderService;
    WALTopicReader walTopicReader;
    WALTopicReader.WALTopicStream walTopicStream;

    @BeforeMethod
    public void setUpMethod() throws Exception {

        wal = new RowColumnValueStoreImpl();
        sipWAL = new RowColumnValueStoreImpl();
        cursors = new RowColumnValueStoreImpl();

        RCVSWALStorage storage = new RCVSWALStorage() {

            @Override
            public RowColumnValueStore<String, Integer, Long, WALEntry, ? extends Exception> getWAL() {
                return wal;
            }

            @Override
            public RowColumnValueStore<String, Integer, Long, SipWALEntry, ? extends Exception> getSipWAL() {
                return sipWAL;
            }

            @Override
            public RowColumnValueStore<String, Integer, Long, Long, ? extends Exception> getCursors() {
                return cursors;
            }
        };

        RCVSWALWriterInitializer.RCVSWALWriterConfig writerConfig = BindInterfaceToConfiguration.bindDefault(RCVSWALWriterInitializer.RCVSWALWriterConfig.class);
        writerConfig.setNumberOfPartitions(10);
        walWriterService = new RCVSWALWriterInitializer().initialize(writerConfig, storage, new RandomParitioningStrategy(new Random(1234)));
        walWriterService.start();
        walWriter = walWriterService.getService();

        PermitProviderImplInitializer.PermitProviderConfig readerPermitsConfig = BindInterfaceToConfiguration.bindDefault(PermitProviderImplInitializer.PermitProviderConfig.class);
        PermitProvider readersPermitProvider = new PermitProviderImplInitializer().initPermitProvider(readerPermitsConfig, new InMemorySetOfSortedMapsImplInitializer());

        WALReadersInitializer.WALReadersConfig readerConfig = BindInterfaceToConfiguration.bindDefault(WALReadersInitializer.WALReadersConfig.class);
        readersService = new WALReadersInitializer().initialize(readerConfig, readersPermitProvider);
        readersService.start();
        readers = readersService.getService();

        RCVSWALCursorStoreInitializer.RCVSWALCursorStoreConfig walCursorStorageConfig = BindInterfaceToConfiguration.bindDefault(RCVSWALCursorStoreInitializer.RCVSWALCursorStoreConfig.class);
        WALService<WALCursorStore> walCursorStorageService = new RCVSWALCursorStoreInitializer().initialize(walCursorStorageConfig, storage);
        walCursorStorageService.start();
        WALCursorStore walCursorStore = walCursorStorageService.getService();

        PermitProviderImplInitializer.PermitProviderConfig cursorPermitsConfig = BindInterfaceToConfiguration.bindDefault(PermitProviderImplInitializer.PermitProviderConfig.class);
        PermitProvider cursroPermitProvider = new PermitProviderImplInitializer().initPermitProvider(cursorPermitsConfig, new InMemorySetOfSortedMapsImplInitializer());

        WALTopicsInitializer.WALTopicsConfig walTopicsConfig = BindInterfaceToConfiguration.bindDefault(WALTopicsInitializer.WALTopicsConfig.class);
        WALService<WALTopics> walTopcisService = new WALTopicsInitializer().initialize(walTopicsConfig, readers, cursroPermitProvider, new ConstantPermitConfig("booya", 0, 10, 1000), walCursorStore);
        walTopcisService.start();
        WALTopics walTopics = walTopcisService.getService();

        walTopicStream = new WALTopicReader.WALTopicStream() {

            @Override
            public void stream(List<WALEntry> entries) {
                System.out.println("consumed" + entries);
            }
        };

        RCVSWALReaderInitializer.RCVSWALTopicReaderConfig topicReaderConfig = BindInterfaceToConfiguration.bindDefault(RCVSWALReaderInitializer.RCVSWALTopicReaderConfig.class);
        topicReaderConfig.setTopicId("booya");
        walTopicReaderService = new RCVSWALReaderInitializer().initialize(topicReaderConfig, storage, walTopics, new IncludeAnyFilter(), walTopicStream);
        walTopicReaderService.start();
        walTopicReader = walTopicReaderService.getService();

    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        walWriterService.stop();
        readersService.stop();
        walTopicReaderService.stop();
    }

    @Test
    public void testWritesAndRead() throws Exception {
        for (int i = 0; i < 100; i++) {
            walWriter.write("booya", Arrays.asList(makeEntry(i)));
        }

        Thread.sleep(10000);

        for (int i = 50; i < 150; i++) {
            walWriter.write("booya", Arrays.asList(makeEntry(i)));
        }

        Thread.sleep(10000);

    }

    private WALEntry makeEntry(int id) {
        return new WALEntry(id, System.currentTimeMillis(), new WALKey(("key-" + id).getBytes()), new WALPayload(("payload-" + id).getBytes()));
    }

}
