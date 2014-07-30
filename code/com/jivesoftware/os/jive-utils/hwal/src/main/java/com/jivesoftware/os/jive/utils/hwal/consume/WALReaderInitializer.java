package com.jivesoftware.os.jive.utils.hwal.consume;

import com.jivesoftware.os.jive.utils.hwal.shared.WAL;
import com.jivesoftware.os.jive.utils.hwal.shared.WALService;
import org.merlin.config.Config;

/**
 *
 * @author jonathan.colt
 */
public class WALReaderInitializer {

    static public interface WALReaderConfig extends Config {

        public String getGroup();
    }

    public WALService<WALReader> initialize(WALReaderConfig config, WAL wal) {
        final WALReader walReader = null;
        return new WALService<WALReader>() {

            @Override
            public WALReader getService() {
                return walReader;
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
