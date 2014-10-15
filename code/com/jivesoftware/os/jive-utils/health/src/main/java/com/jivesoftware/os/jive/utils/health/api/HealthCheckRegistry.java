package com.jivesoftware.os.jive.utils.health.api;

/**
 *
 * @author jonathan.colt
 */
public interface HealthCheckRegistry {

    void register(HealthChecker healthChecker);

    void unregister(HealthChecker healthChecker);
}
