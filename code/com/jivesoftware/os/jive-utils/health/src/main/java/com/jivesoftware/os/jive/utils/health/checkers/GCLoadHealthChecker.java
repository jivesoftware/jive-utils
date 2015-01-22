package com.jivesoftware.os.jive.utils.health.checkers;

import com.jivesoftware.os.jive.utils.health.api.MinMaxHealthCheckConfig;
import com.jivesoftware.os.jive.utils.health.api.MinMaxHealthChecker;
import com.jivesoftware.os.jive.utils.health.api.ScheduledHealthCheck;
import com.jivesoftware.os.mlogger.core.Counter;
import com.jivesoftware.os.mlogger.core.ValueType;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;
import org.merlin.config.defaults.LongDefault;
import org.merlin.config.defaults.StringDefault;

/**
 *
 * @author jonathan.colt
 */
public class GCLoadHealthChecker extends MinMaxHealthChecker implements ScheduledHealthCheck {

    static public interface GCLoadHealthCheckerConfig extends MinMaxHealthCheckConfig {

        @StringDefault ("jvm>gc>load")
        @Override
        String getName();

        @LongDefault (1_000)
        Long getCheckIntervalInMillis();

        @LongDefault (10)
        @Override
        Long getMax();

        @StringDefault("Amount of time CPU is spending in GC as reported by GarbageCollectorMXBean.")
        @Override
        String getDescription();
    }

    private final GCLoadHealthCheckerConfig config;
    private final List<GarbageCollectorMXBean> garbageCollectors;
    private long lastTime = System.currentTimeMillis();
    private long lastGCTotalTime;

    public GCLoadHealthChecker(GCLoadHealthCheckerConfig config) {
        super(config);
        this.config = config;
        garbageCollectors = ManagementFactory.getGarbageCollectorMXBeans();
    }

    @Override
    public long getCheckIntervalInMillis() {
        return config.getCheckIntervalInMillis();
    }

    @Override
    public void run() {
        try {

            long totalTimeInGC = 0;
            int availableProcessors = Runtime.getRuntime().availableProcessors();
            for (GarbageCollectorMXBean gc : garbageCollectors) {
                totalTimeInGC += gc.getCollectionTime();
            }


            long timeSpendInGC = totalTimeInGC - lastGCTotalTime;
            long cpuTimeSinceLastSample = (System.currentTimeMillis() - lastTime) * availableProcessors;
            double percentageOfCPUTimeInGC = ((float) (timeSpendInGC) / (float) cpuTimeSinceLastSample);

            lastGCTotalTime = totalTimeInGC;
            lastTime = System.currentTimeMillis();

            Counter counter = new Counter(ValueType.RATE);
            counter.set((int) (percentageOfCPUTimeInGC * 100));
            check(counter, config.getDescription(), "Consider tuning GC parameters.");
        } catch (Exception x) {
            // TODO what?
        }
    }

}
