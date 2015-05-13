package com.jivesoftware.os.jive.utils.health.checkers;

import com.jivesoftware.os.jive.utils.health.HealthCheckResponse;
import com.jivesoftware.os.jive.utils.health.HealthCheckResponseImpl;
import com.jivesoftware.os.jive.utils.health.api.HealthCheckUtil;
import com.jivesoftware.os.jive.utils.health.api.HealthChecker;
import com.jivesoftware.os.jive.utils.health.api.HealthFactory;
import com.jivesoftware.os.jive.utils.health.api.TimerHealthCheckConfig;
import com.jivesoftware.os.mlogger.core.Timer;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author jonathan.colt
 */
public class TimerHealthChecker implements HealthChecker<Timer> {

    static public HealthFactory.HealthCheckerConstructor<Timer, TimerHealthCheckConfig> FACTORY =
        new HealthFactory.HealthCheckerConstructor<Timer, TimerHealthCheckConfig>() {
            @Override
            public HealthChecker<Timer> construct(TimerHealthCheckConfig config) {
                return new TimerHealthChecker(config);
            }
        };

    private final TimerHealthCheckConfig config;
    private final AtomicReference<Callable<HealthCheckResponse>> lastTimer = new AtomicReference<>();

    public TimerHealthChecker(TimerHealthCheckConfig config) {
        this.config = config;
    }

    @Override
    public void check(final Timer timer, final String description, final String resolution) {
        final long time = System.currentTimeMillis();
        lastTimer.set(new Callable<HealthCheckResponse>() {

            @Override
            public HealthCheckResponse call() throws Exception {
                double health = 1.0f;
                health = Math.min(health, HealthCheckUtil.zeroToOne(config.getMeanMax(), 0, timer.getMean()));
                health = Math.min(health, HealthCheckUtil.zeroToOne(config.getVarianceMax(), 0, Math.abs(timer.getVariance())));
                health = Math.min(health, HealthCheckUtil.zeroToOne(config.get50ThPecentileMax(), 0, timer.get50ThPercentile()));
                health = Math.min(health, HealthCheckUtil.zeroToOne(config.get75ThPecentileMax(), 0, timer.get75ThPercentile()));
                health = Math.min(health, HealthCheckUtil.zeroToOne(config.get90ThPecentileMax(), 0, timer.get90ThPercentile()));
                health = Math.min(health, HealthCheckUtil.zeroToOne(config.get95ThPecentileMax(), 0, timer.get95ThPercentile()));
                health = Math.min(health, HealthCheckUtil.zeroToOne(config.get99ThPecentileMax(), 0, timer.get99ThPercentile()));
                return new HealthCheckResponseImpl(config.getName(), health, healthString(timer), description, resolution, time);
            }
        });
    }

    @Override
    public HealthCheckResponse checkHealth() throws Exception {
        Callable<HealthCheckResponse> callable = lastTimer.get();
        if (callable != null) {
            return callable.call();
        } else {
            return new HealthCheckResponseImpl(config.getName(),
                HealthCheckResponse.UNKNOWN,
                "Health is currently unknown for this check.",
                "Description is currently unknown for this check.",
                "Resolution is currently unknown for this check.",
                System.currentTimeMillis());
        }
    }

    private String healthString(Timer timer) {
        StringBuilder sb = new StringBuilder();
        sb.append(" samples:").append(timer.getSampleCount());
        sb.append(" mean:").append(timer.getMean());
        sb.append(" variance:").append(timer.getVariance());
        sb.append(" 50th:").append(timer.get50ThPercentile());
        sb.append(" 57th:").append(timer.get75ThPercentile());
        sb.append(" 90th:").append(timer.get90ThPercentile());
        sb.append(" 95th:").append(timer.get95ThPercentile());
        sb.append(" 99th:").append(timer.get99ThPercentile());
        return sb.toString();
    }

}
