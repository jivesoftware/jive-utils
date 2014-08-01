package com.jivesoftware.os.jive.utils.hwal.shared.rcvs;

import com.jivesoftware.os.jive.utils.hwal.shared.api.SipWALEntry;
import com.jivesoftware.os.jive.utils.hwal.shared.api.WALEntry;
import com.jivesoftware.os.jive.utils.hwal.shared.api.WALService;
import com.jivesoftware.os.jive.utils.hwal.shared.marshall.SipWALEntryMarshaller;
import com.jivesoftware.os.jive.utils.hwal.shared.marshall.WALEntryMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.DefaultRowColumnValueStoreMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.SetOfSortedMapsImplInitializer;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.CurrentTimestamper;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.primatives.IntegerTypeMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.primatives.LongTypeMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.primatives.StringTypeMarshaller;
import java.io.IOException;
import org.merlin.config.Config;
import org.merlin.config.defaults.StringDefault;

/**
 * I
 *
 * @author jonathan.colt
 */
public class RCVSWALStorageInitializer {

    static public interface RCVSWALStorageConfig extends Config {

        @StringDefault("dev")
        public String getTableNameSpace();
    }

    public WALService<RCVSWALStorage> initialize(RCVSWALStorageConfig config, SetOfSortedMapsImplInitializer sortedMapsImplInitializer) throws IOException {

        final RowColumnValueStore<String, Integer, Long, WALEntry, ? extends Exception> wal = sortedMapsImplInitializer.initialize(
                config.getTableNameSpace(), "hwal.master.wal", "cf",
                new DefaultRowColumnValueStoreMarshaller(new StringTypeMarshaller(),
                        new IntegerTypeMarshaller(),
                        new LongTypeMarshaller(),
                        new WALEntryMarshaller()), new CurrentTimestamper());

        final RowColumnValueStore<String, Integer, Long, SipWALEntry, ? extends Exception> sipWAL = sortedMapsImplInitializer.initialize(
                config.getTableNameSpace(), "hwal.sip.wal", "cf",
                new DefaultRowColumnValueStoreMarshaller(new StringTypeMarshaller(),
                        new IntegerTypeMarshaller(),
                        new LongTypeMarshaller(),
                        new SipWALEntryMarshaller()), new CurrentTimestamper());

        final RowColumnValueStore<String, Integer, Long, Long, ? extends Exception> cursors = sortedMapsImplInitializer.initialize(
                config.getTableNameSpace(), "hwal.cursors", "cf",
                new DefaultRowColumnValueStoreMarshaller(new StringTypeMarshaller(),
                        new IntegerTypeMarshaller(),
                        new LongTypeMarshaller(),
                        new LongTypeMarshaller()), new CurrentTimestamper());

        final RCVSWALStorage storage = new RCVSWALStorage() {

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

        return new WALService<RCVSWALStorage>() {

            @Override
            public RCVSWALStorage getService() {
                return storage;
            }

            @Override
            public void start() throws Exception {
            }

            @Override
            public void stop() throws Exception {
            }
        };
    }
}
