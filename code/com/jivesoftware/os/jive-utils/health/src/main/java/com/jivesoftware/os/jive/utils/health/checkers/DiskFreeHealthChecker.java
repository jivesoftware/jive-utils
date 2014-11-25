package com.jivesoftware.os.jive.utils.health.checkers;

import com.jivesoftware.os.jive.utils.health.api.HealthCheckUtil;
import com.jivesoftware.os.jive.utils.health.api.MinMaxHealthChecker;
import com.jivesoftware.os.jive.utils.health.api.ScheduledHealthCheck;
import com.jivesoftware.os.jive.utils.health.api.ScheduledMinMaxHealthCheckConfig;
import com.jivesoftware.os.jive.utils.logger.Counter;
import com.jivesoftware.os.jive.utils.logger.ValueType;
import java.io.File;

/**
 *
 * @author jonathan.colt
 */
public class DiskFreeHealthChecker extends MinMaxHealthChecker implements ScheduledHealthCheck {

    private final ScheduledMinMaxHealthCheckConfig config;
    private final File[] paths;

    public DiskFreeHealthChecker(ScheduledMinMaxHealthCheckConfig config, File[] paths) {
        super(config);
        this.config = config;
        this.paths = paths;
    }

    @Override
    public long getCheckIntervalInMillis() {
        return config.getCheckIntervalInMillis();
    }

    @Override
    public void run() {
        try {
            StringBuilder sb = new StringBuilder();
            double worstHealth = 1.0d;
            for (File path : paths) {
                double percentageUsed = HealthCheckUtil.zeroToOne(0, path.getTotalSpace(), path.getTotalSpace() - path.getUsableSpace());
                sb.append("path:").append(path).append(" at ").append(100 * percentageUsed).append("% used. ");
                if (percentageUsed < worstHealth) {
                    worstHealth = percentageUsed;
                }
            }
            Counter counter = new Counter(ValueType.RATE);
            counter.set((int) (worstHealth * 100));
            check(counter, sb.toString(), "Remove some unused files or get more or larger drive.");
        } catch (Exception x) {
            // TODO what?
        }
    }

}
