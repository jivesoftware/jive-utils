package com.jivesoftware.os.jive.utils.hwal.write.rcvs;

import com.jivesoftware.os.jive.utils.hwal.shared.api.WALService;
import com.jivesoftware.os.jive.utils.hwal.shared.partition.WALPartitioningStrategy;
import com.jivesoftware.os.jive.utils.hwal.shared.rcvs.RCVSWALStorage;
import com.jivesoftware.os.jive.utils.hwal.write.SipWALTimeProvider;
import com.jivesoftware.os.jive.utils.hwal.write.WALWriter;
import com.jivesoftware.os.jive.utils.ordered.id.JiveEpochTimestampProvider;
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
            WALPartitioningStrategy partitioningStrategy) {

        SipWALTimeProvider sipWALTimeProvider = new SipWALTimeProvider(new JiveEpochTimestampProvider());

        final WALWriter cursorStore = new RCVSWALWriter(sipWALTimeProvider,
                storage.getWAL(),
                storage.getSipWAL(),
                partitioningStrategy,
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
