package com.jivesoftware.os.jive.utils.health.checkers;

import com.jivesoftware.os.jive.utils.health.api.MinMaxHealthCheckConfig;
import com.jivesoftware.os.jive.utils.health.api.MinMaxHealthChecker;
import com.jivesoftware.os.jive.utils.health.api.ScheduledHealthCheck;
import com.jivesoftware.os.jive.utils.logger.Counter;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import com.jivesoftware.os.jive.utils.logger.ValueType;
import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import org.merlin.config.defaults.LongDefault;
import org.merlin.config.defaults.StringDefault;

/**
 *
 */
public class DirectBufferHealthChecker extends MinMaxHealthChecker implements ScheduledHealthCheck {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();

    static public interface DirectBufferHealthCheckerConfig extends MinMaxHealthCheckConfig {

        @StringDefault("jvm>directBuffers>usage")
        @Override
        String getName();

        @StringDefault("Total off heap memory used as declared by -XX:MaxDirectMemorySize")
        @Override
        String getDescription();

        @LongDefault(10_000)
        Long getCheckIntervalInMillis();

        @LongDefault(-1) // -1 means normalize to 90% of command line argument
        @Override
        Long getMax();

        void setMax(Long max);
    }

    private final DirectBufferHealthCheckerConfig config;
    private final List<BufferPoolMXBean> bufferPools;

    private static DirectBufferHealthCheckerConfig normalize(DirectBufferHealthCheckerConfig config) {
        if (config.getMax() == -1) {
            RuntimeMXBean RuntimemxBean = ManagementFactory.getRuntimeMXBean();
            List<String> args = RuntimemxBean.getInputArguments();

            long maxDirectMemorySize = -1;
            for (String arg : args) {
                if (arg.startsWith("-XX:MaxDirectMemorySize=")) {
                    String size = arg.substring(arg.indexOf('=') + 1).toLowerCase();
                    int num = Integer.parseInt(size.substring(0, size.length() - 1));
                    String unit = size.substring(size.length() - 1);
                    long unitMultiplier;
                    switch (unit) {
                        case "k":
                            unitMultiplier = 1_024;
                            break;
                        case "m":
                            unitMultiplier = 1_024 * 1_024;
                            break;
                        case "g":
                            unitMultiplier = 1_024 * 1_024 * 1_024;
                            break;
                        default:
                            unitMultiplier = 1;
                    }
                    maxDirectMemorySize = unitMultiplier * num;
                }
            }

            if (maxDirectMemorySize >= 0) {
                config.setMax(maxDirectMemorySize * 9 / 10);
            }
        }
        LOG.info("Max is {}", config.getMax());
        return config;
    }

    public DirectBufferHealthChecker(DirectBufferHealthCheckerConfig config) {
        super(normalize(config));
        this.config = config;
        bufferPools = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
    }

    @Override
    public long getCheckIntervalInMillis() {
        return config.getCheckIntervalInMillis();
    }

    @Override
    public void run() {
        try {
            long memoryUsed = 0;
            for (BufferPoolMXBean bufferPool : bufferPools) {
                if (bufferPool.getName().equals("direct")) {
                    memoryUsed = bufferPool.getMemoryUsed();
                }
            }

            Counter counter = new Counter(ValueType.VALUE);
            counter.set(memoryUsed);
            check(counter, config.getDescription(), "Allocate more direct memory. ");
        } catch (Exception x) {
            // TODO what?
        }
    }

}
