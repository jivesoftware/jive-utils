package com.jivesoftware.os.jive.utils.health.api;

import com.jivesoftware.os.jive.utils.logger.CountersAndTimers;
import java.util.concurrent.ScheduledExecutorService;

/**
 *
 * @author jonathan.colt
 */
public class HealthFactory {

    static HealthCheckConfigBinder configBinder;
    static HealthCheckRegistry healthCheckRegistry;
    static ScheduledExecutorService scheduledHealthChecks;
    static CountersAndTimers countersAndTimers = CountersAndTimers.getOrCreate("health");

    static public void initialize(HealthCheckConfigBinder _configBinder,
        HealthCheckRegistry _healthCheckRegistry) {
        configBinder = _configBinder;
        healthCheckRegistry = _healthCheckRegistry;
    }

    static public interface HealthCheckerConstructor<T, C> {

        HealthChecker<T> construct(C config);
    }

    static public <T, C extends ScheduledMinMaxHealthCheckConfig> void scheduleHealthChecker(
        Class<C> healthCheckConfig, HealthCheckerConstructor<T, C> constructor) {

        C config = configBinder.bindConfig(healthCheckConfig);
        HealthChecker checker = constructor.construct(config);
        healthCheckRegistry.register(checker);
    }

    static public <T, C extends HealthCheckConfig> HealthCounter getHealthCounter(
        Class<C> healthCheckConfig, HealthCheckerConstructor<T, C> constructor) {

        C config = configBinder.bindConfig(healthCheckConfig);
        HealthChecker checker = constructor.construct(config);
        healthCheckRegistry.register(checker);
        return new HealthCounter(countersAndTimers,
            config.getName(),
            checker
        );
    }

    static public <T, D extends HealthCheckConfig, C extends D> HealthTimer getHealthTimer(
        Class<C> healthCheckConfig, HealthCheckerConstructor<T, D> constructor) {

        C config = configBinder.bindConfig(healthCheckConfig);
        HealthChecker checker = constructor.construct(config);
        healthCheckRegistry.register(checker);
        return new HealthTimer(countersAndTimers,
            config.getName(),
            checker
        );
    }

}
