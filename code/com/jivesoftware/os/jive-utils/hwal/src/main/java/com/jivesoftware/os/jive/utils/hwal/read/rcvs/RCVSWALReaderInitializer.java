package com.jivesoftware.os.jive.utils.hwal.read.rcvs;

import com.jivesoftware.os.jive.utils.hwal.read.WALReader;
import com.jivesoftware.os.jive.utils.hwal.shared.api.WALService;
import org.merlin.config.Config;

/**
 *I
 * @author jonathan.colt
 */
public class RCVSWALReaderInitializer {

    static public interface RCVSWALReaderConfig extends Config {

        public String getTopic();

        public int getNumberOfPartitions();
    }

    public WALService<WALReader> initialize(RCVSWALReaderConfig config) {
        final WALReader wal = null;
        return new WALService<WALReader>() {

            @Override
            public WALReader getService() {
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
