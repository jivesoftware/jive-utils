package com.jivesoftware.os.jive.utils.health.api;

import com.jivesoftware.os.jive.utils.logger.Counter;
import com.jivesoftware.os.jive.utils.logger.CountersAndTimers;
import com.jivesoftware.os.jive.utils.logger.Timer;

/**
 * @author jonathan.colt
 */
public class HealthFactory {

    static HealthCheckConfigBinder configBinder;
    static HealthCheckRegistry healthCheckRegistry;
    static CountersAndTimers countersAndTimers = CountersAndTimers.getOrCreate("health");

    static public void initialize(HealthCheckConfigBinder _configBinder,
        HealthCheckRegistry _healthCheckRegistry) {
        configBinder = _configBinder;
        healthCheckRegistry = _healthCheckRegistry;
    }

    static public interface HealthCheckerConstructor<T, C> {

        HealthChecker<T> construct(C config);
    }

    static public <C extends ScheduledMinMaxHealthCheckConfig> void scheduleHealthChecker(
        Class<C> healthCheckConfig, HealthCheckerConstructor<?, C> constructor) {

        C config = configBinder.bindConfig(healthCheckConfig);
        HealthChecker<?> checker = constructor.construct(config);
        healthCheckRegistry.register(checker);
    }

    static public <D extends HealthCheckConfig, C extends D> HealthCounter getHealthCounter(
        Class<C> healthCheckConfig, HealthCheckerConstructor<Counter, D> constructor) {

        C config = configBinder.bindConfig(healthCheckConfig);
        HealthChecker<Counter> checker = constructor.construct(config);
        healthCheckRegistry.register(checker);
        return new HealthCounter(countersAndTimers,
            config.getName(),
            checker
        );
    }

    static public <D extends HealthCheckConfig, C extends D> HealthTimer getHealthTimer(
        Class<C> healthCheckConfig, HealthCheckerConstructor<Timer, D> constructor) {

        C config = configBinder.bindConfig(healthCheckConfig);
        HealthChecker<Timer> checker = constructor.construct(config);
        healthCheckRegistry.register(checker);
        return new HealthTimer(countersAndTimers,
            config.getName(),
            checker
        );
    }

}
