package com.jivesoftware.os.jive.utils.hwal.produce;

import com.jivesoftware.os.jive.utils.hwal.shared.WAL;
import com.jivesoftware.os.jive.utils.hwal.shared.WALService;
import org.merlin.config.Config;

/**
 *
 * @author jonathan.colt
 */
public class WALWriterInitializer {

    static public interface WALWriterConfig extends Config {

        public String getGroup();
    }

    public WALService<WALWriter> initialize(WALWriterConfig config, WAL wal) {
        final WALWriter walWriter = null;
        return new WALService<WALWriter>() {

            @Override
            public WALWriter getService() {
                return walWriter;
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
