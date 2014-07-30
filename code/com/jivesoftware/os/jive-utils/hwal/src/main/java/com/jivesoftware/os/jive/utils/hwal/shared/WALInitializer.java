package com.jivesoftware.os.jive.utils.hwal.shared;

import org.merlin.config.Config;

/**
 *
 * @author jonathan.colt
 */
public class WALInitializer {

    static public interface WALConfig extends Config {

        public String getTopic();

        public int getNumberOfPartitions();
    }

    public WALService<WAL> initialize(WALConfig config) {
        final WAL wal = null;
        return new WALService<WAL>() {

            @Override
            public WAL getService() {
                return wal;
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
