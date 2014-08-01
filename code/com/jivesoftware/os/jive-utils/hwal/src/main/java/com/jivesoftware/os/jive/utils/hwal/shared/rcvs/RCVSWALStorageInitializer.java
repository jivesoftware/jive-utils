package com.jivesoftware.os.jive.utils.hwal.shared.rcvs;

import com.jivesoftware.os.jive.utils.hwal.shared.api.SipWALEntry;
import com.jivesoftware.os.jive.utils.hwal.shared.api.SipWALTime;
import com.jivesoftware.os.jive.utils.hwal.shared.api.WALEntry;
import com.jivesoftware.os.jive.utils.hwal.shared.api.WALService;
import com.jivesoftware.os.jive.utils.hwal.shared.marshall.SipWALEntryMarshaller;
import com.jivesoftware.os.jive.utils.hwal.shared.marshall.SipWALTimeMarshaller;
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

        final RowColumnValueStore<String, Integer, Long, WALEntry, ? extends RuntimeException> wal = sortedMapsImplInitializer.initialize(
                config.getTableNameSpace(), "hwal.master.wal", "cf",
                new DefaultRowColumnValueStoreMarshaller(new StringTypeMarshaller(),
                        new IntegerTypeMarshaller(),
                        new LongTypeMarshaller(),
                        new WALEntryMarshaller()), new CurrentTimestamper());

        final RowColumnValueStore<String, Integer, SipWALTime, SipWALEntry, ? extends RuntimeException> sipWAL = sortedMapsImplInitializer.initialize(
                config.getTableNameSpace(), "hwal.sip.wal", "cf",
                new DefaultRowColumnValueStoreMarshaller(new StringTypeMarshaller(),
                        new IntegerTypeMarshaller(),
                        new SipWALTimeMarshaller(),
                        new SipWALEntryMarshaller()), new CurrentTimestamper());

        final RowColumnValueStore<String, String, Integer, Long, ? extends RuntimeException> cursors = sortedMapsImplInitializer.initialize(
                config.getTableNameSpace(), "hwal.cursors", "cf",
                new DefaultRowColumnValueStoreMarshaller(new StringTypeMarshaller(),
                        new StringTypeMarshaller(),
                        new IntegerTypeMarshaller(),
                        new LongTypeMarshaller()), new CurrentTimestamper());

        final RCVSWALStorage storage = new RCVSWALStorage() {

            @Override
            public RowColumnValueStore<String, Integer, Long, WALEntry, ? extends RuntimeException> getWAL() {
                return wal;
            }

            @Override
            public RowColumnValueStore<String, Integer, SipWALTime, SipWALEntry, ? extends RuntimeException> getSipWAL() {
                return sipWAL;
            }

            @Override
            public RowColumnValueStore<String, String, Integer, Long, ? extends RuntimeException> getCursors() {
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
