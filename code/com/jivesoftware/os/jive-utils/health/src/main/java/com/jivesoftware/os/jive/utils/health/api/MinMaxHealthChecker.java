package com.jivesoftware.os.jive.utils.health.api;

import com.jivesoftware.os.jive.utils.health.HealthCheckResponse;
import com.jivesoftware.os.jive.utils.health.HealthCheckResponseImpl;
import com.jivesoftware.os.jive.utils.logger.Counter;
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
    public void check(final Counter counter, final String context) {
        check.set(new Callable<HealthCheckResponse>() {

            @Override
            public HealthCheckResponse call() throws Exception {

                double health = 1.0d;
                health = Math.min(health, HealthCheckUtil.zeroToOne(config.getMax(), config.getMin(), counter.getCount()));
                if (health > 1.0d || health < 0.0d) {
                    health = 0.0d;
                }
                return new HealthCheckResponseImpl(config.getName(),
                    health, healthString(counter) + " " + context,
                    System.currentTimeMillis());
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
                HealthCheckResponse.UNKNOWN, "Health is currently unknown for this check.",
                System.currentTimeMillis());
        }
    }

    private String healthString(Counter counter) {
        StringBuilder sb = new StringBuilder();
        sb.append(" min:" + config.getMin());
        sb.append(" count:" + counter.getCount());
        sb.append(" max:" + config.getMax());
        return sb.toString();
    }

}
