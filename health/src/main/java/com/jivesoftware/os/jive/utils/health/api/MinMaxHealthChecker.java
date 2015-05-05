package com.jivesoftware.os.jive.utils.health.api;

import com.jivesoftware.os.jive.utils.health.HealthCheckResponse;
import com.jivesoftware.os.jive.utils.health.HealthCheckResponseImpl;
import com.jivesoftware.os.mlogger.core.Counter;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author jonathan.colt
 */
public class MinMaxHealthChecker implements HealthChecker<Counter> {

    public static HealthFactory.HealthCheckerConstructor<Counter, MinMaxHealthCheckConfig> FACTORY =
        new HealthFactory.HealthCheckerConstructor<Counter, MinMaxHealthCheckConfig>() {
            @Override
            public HealthChecker<Counter> construct(MinMaxHealthCheckConfig config) {
                return new MinMaxHealthChecker(config);
            }
        };

    private final MinMaxHealthCheckConfig config;
    private final AtomicReference<Callable<HealthCheckResponse>> check = new AtomicReference<>();

    public MinMaxHealthChecker(MinMaxHealthCheckConfig config) {
        this.config = config;
    }

    @Override
    public void check(final Counter counter, final String description, final String resolution) {
        final long time = System.currentTimeMillis();
        check.set(new Callable<HealthCheckResponse>() {

            @Override
            public HealthCheckResponse call() throws Exception {

                double health = 1.0d;
                health = Math.min(health, HealthCheckUtil.zeroToOne(config.getMax(), config.getMin(), counter.getCount()));
                if (health > 1.0d || health < 0.0d) {
                    health = 0.0d;
                }
                return new HealthCheckResponseImpl(config.getName(),
                    health,
                    healthString(counter),
                    description,
                    resolution,
                    time);
            }
        });
    }

    @Override
    public HealthCheckResponse checkHealth() throws Exception {
        Callable<HealthCheckResponse> callable = check.get();
        if (callable != null) {
            return callable.call();
        } else {
            return new HealthCheckResponseImpl(config.getName(),
                HealthCheckResponse.UNKNOWN,
                "Health is currently unknown for this check.",
                "Description is currently unknown for this check.",
                "Resolution is currently unkown for this check.",
                System.currentTimeMillis());
        }
    }

    private String healthString(Counter counter) {
        StringBuilder sb = new StringBuilder();
        sb.append(" min:").append(config.getMin());
        sb.append(" count:").append(counter.getCount());
        sb.append(" max:").append(config.getMax());
        return sb.toString();
    }

}
