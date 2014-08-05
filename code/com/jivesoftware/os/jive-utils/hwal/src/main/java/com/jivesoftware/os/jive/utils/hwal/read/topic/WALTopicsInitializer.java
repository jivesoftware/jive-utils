package com.jivesoftware.os.jive.utils.hwal.read.topic;

import com.jivesoftware.os.jive.utils.hwal.read.WALCursorStore;
import com.jivesoftware.os.jive.utils.hwal.read.WALReaders;
import com.jivesoftware.os.jive.utils.hwal.shared.api.WALService;
import com.jivesoftware.os.jive.utils.permit.PermitConfig;
import com.jivesoftware.os.jive.utils.permit.PermitProvider;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.merlin.config.Config;
import org.merlin.config.defaults.IntDefault;

/**
 * I
 *
 * @author jonathan.colt
 */
public class WALTopicsInitializer {

    static public interface WALTopicsConfig extends Config {

        @IntDefault(10)
        public int getNumberOfPartitions();

        public void setNumberOfPartitions(int numberOfPartitions);

        @IntDefault(100)
        public int getRebalanceIntervalInMillis();

        public void setRebalanceIntervalInMillis(int millis);

    }

    public WALService<WALTopics> initialize(final WALTopicsConfig config,
            WALReaders readers,
            PermitProvider topicCursorPermitProvider,
            PermitConfig topicCursorPermitConfig,
            WALCursorStore cursorStore) {

        final WALTopics topics = new WALTopics(readers, topicCursorPermitProvider, topicCursorPermitConfig, cursorStore);

        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        return new WALService<WALTopics>() {

            @Override
            public WALTopics getService() {
                return topics;
            }

            @Override
            public void start() throws Exception {
                executorService.scheduleAtFixedRate(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            topics.online();
                        } catch (Exception x) {
                            x.printStackTrace();
                        }
                    }
                }, 0, config.getRebalanceIntervalInMillis(), TimeUnit.MILLISECONDS);
            }

            @Override
            public void stop() throws Exception {
                executorService.shutdownNow();
                try {
                    topics.offline();
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        };
    }
}
