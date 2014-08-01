package com.jivesoftware.os.jive.utils.hwal.read.rcvs;

import com.jivesoftware.os.jive.utils.hwal.read.WALTopicReader;
import com.jivesoftware.os.jive.utils.hwal.read.WALTopicReader.WALTopicStream;
import com.jivesoftware.os.jive.utils.hwal.read.partitions.WALTopicCursors;
import com.jivesoftware.os.jive.utils.hwal.read.topic.WALTopics;
import com.jivesoftware.os.jive.utils.hwal.shared.api.WALService;
import com.jivesoftware.os.jive.utils.hwal.shared.filter.WALKeyFilter;
import com.jivesoftware.os.jive.utils.hwal.shared.rcvs.RCVSWALStorage;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.merlin.config.Config;
import org.merlin.config.defaults.IntDefault;
import org.merlin.config.defaults.StringDefault;

/**
 * I
 *
 * @author jonathan.colt
 */
public class RCVSWALReaderInitializer {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();

    static public interface RCVSWALTopicReaderConfig extends Config {

        @StringDefault("unspecifiedTopicId")
        public String getTopicId();

        public void setTopicId(String topicId);

        @IntDefault(10)
        public int getBatchSize();

        public void setBatchSize(int batchSize);

        @IntDefault(10)
        public int getNumberOfPartitions();

        public void setNumberOfPartitions(int numberOfPartitions);

        @IntDefault(300)
        public int getPollEmptyPartitionIntervalMillis();

        public void setPollEmptyPartitionIntervalMillis(int millis);

        @IntDefault(300)
        public int getMaxClockDriptMillis();

        public void setMaxClockDriptMillis(int millis);

    }

    public WALService<WALTopicReader> initialize(final RCVSWALTopicReaderConfig config,
            RCVSWALStorage storage,
            final WALTopics topics,
            final WALKeyFilter filter,
            final WALTopicStream stream) {

        final WALTopicCursors walTopicCursors = topics.getWALTopicCursors(config.getTopicId(), config.getNumberOfPartitions());

        final WALTopicReader walReader = new RCVSWALTopicReader(storage.getWAL(),
                storage.getSipWAL(),
                walTopicCursors,
                config.getPollEmptyPartitionIntervalMillis(), config.getMaxClockDriptMillis());
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        return new WALService<WALTopicReader>() {

            @Override
            public WALTopicReader getService() {
                return walReader;
            }

            @Override
            public void start() throws Exception {
                executorService.submit(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            walReader.stream(filter, config.getBatchSize(), stream);
                        } catch (Exception x) {
                            LOG.error("WAL Reader for "+config+" shutdown by a failure to handle.", x);
                        }
                    }
                });
            }

            @Override
            public void stop() throws Exception {
                executorService.shutdownNow();
                topics.removeWALTopicCursors(config.getTopicId());
            }
        };
    }
}
