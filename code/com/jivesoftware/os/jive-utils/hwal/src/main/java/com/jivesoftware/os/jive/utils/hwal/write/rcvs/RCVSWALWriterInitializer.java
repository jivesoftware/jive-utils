package com.jivesoftware.os.jive.utils.hwal.write.rcvs;

import com.jivesoftware.os.jive.utils.hwal.shared.api.WALService;
import com.jivesoftware.os.jive.utils.hwal.shared.partition.WALParitioningStrategy;
import com.jivesoftware.os.jive.utils.hwal.shared.rcvs.RCVSWALStorage;
import com.jivesoftware.os.jive.utils.hwal.write.WALWriter;
import org.merlin.config.Config;
import org.merlin.config.defaults.IntDefault;

/**
 * I
 *
 * @author jonathan.colt
 */
public class RCVSWALWriterInitializer {

    static public interface RCVSWALWriterConfig extends Config {

        @IntDefault(20)
        public int getNumberOfPartitions();

        public void setNumberOfPartitions(int numberOfPartitions);

    }

    public WALService<WALWriter> initialize(RCVSWALWriterConfig config,
            RCVSWALStorage storage,
            WALParitioningStrategy paritioningStrategy) {

        final WALWriter cursorStore = new RCVSWALWriter(storage.getWAL(),
                storage.getSipWAL(),
                paritioningStrategy,
                config.getNumberOfPartitions());

        return new WALService<WALWriter>() {

            @Override
            public WALWriter getService() {
                return cursorStore;
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
