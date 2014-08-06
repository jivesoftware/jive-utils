package com.jivesoftware.os.jive.utils.hwal.read;

import com.jivesoftware.os.jive.utils.hwal.shared.api.WALService;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import com.jivesoftware.os.jive.utils.permit.ConstantPermitConfig;
import com.jivesoftware.os.jive.utils.permit.PermitProvider;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.merlin.config.Config;
import org.merlin.config.defaults.IntDefault;
import org.merlin.config.defaults.StringDefault;

public class WALReadersInitializer {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();

    static public interface WALReadersConfig extends Config {

        @StringDefault("defaultGroup")
        public String getReaderGroupId();

        public void setReaderGroupId(String readersGroupId);

        @IntDefault(100)
        public int getHeartbeatIntervalInMillis();

        public void setHeartbeatIntervalInMillis(int millis);
    }

    public WALService<WALReaders> initialize(final WALReadersConfig config, PermitProvider permitProvider) {

        ConstantPermitConfig permitConfig = new ConstantPermitConfig(0, 1000, config.getHeartbeatIntervalInMillis() * 3);

        final WALReaders readers = new WALReaders("WALReaders", config.getReaderGroupId(), permitProvider, permitConfig);
        return new WALService<WALReaders>() {
            ScheduledExecutorService scheduledExecutorService;

            @Override
            public WALReaders getService() {
                return readers;
            }

            @Override
            synchronized public void start() throws Exception {
                if (scheduledExecutorService == null) {
                    scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
                }

                scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            readers.online();
                        } catch (Throwable x) {
                            LOG.error("Failed while bringing readers online:" + x);
                        }
                    }
                }, 0, config.getHeartbeatIntervalInMillis(), TimeUnit.MILLISECONDS);

            }

            @Override
            synchronized public void stop() throws Exception {
                scheduledExecutorService.shutdownNow();
                try {
                    readers.offline();
                } catch (Throwable x) {
                    x.printStackTrace();
                }
                scheduledExecutorService = null;
            }
        };
    }
}
