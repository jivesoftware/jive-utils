package com.jivesoftware.os.jive.utils.hwal.read;

import com.jivesoftware.os.jive.utils.hwal.shared.api.WALService;
import com.jivesoftware.os.jive.utils.permit.ConstantPermitConfig;
import com.jivesoftware.os.jive.utils.permit.PermitProvider;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.merlin.config.Config;
import org.merlin.config.defaults.IntDefault;
import org.merlin.config.defaults.StringDefault;

public class WALReadersInitializer {

    static public interface WALReadersConfig extends Config {

        @StringDefault("defaultGroup")
        public String getReaderGroupId();

        public void setReaderGroupId(String readersGroupId);

        @IntDefault(1000)
        public int getHeartbeatIntervalInMillis();

        public void setHeartbeatIntervalInMillis(int millis);
    }

    public WALService<WALReaders> initialize(final WALReadersConfig config, PermitProvider permitProvider) {

        ConstantPermitConfig permitConfig = new ConstantPermitConfig(config.getReaderGroupId(), 0, 1000, config.getHeartbeatIntervalInMillis() * 2);

        final WALReaders readers = new WALReaders(config.getReaderGroupId(), permitProvider, permitConfig);
        final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        return new WALService<WALReaders>() {

            @Override
            public WALReaders getService() {
                return readers;
            }

            @Override
            public void start() throws Exception {
                scheduledExecutorService.scheduleAtFixedRate(new Runnable() {

                    @Override
                    public void run() {
                        readers.online();
                    }
                }, 0, config.getHeartbeatIntervalInMillis(), TimeUnit.MILLISECONDS);
            }

            @Override
            public void stop() throws Exception {
                scheduledExecutorService.shutdownNow();
                readers.offline();
            }
        };
    }
}
