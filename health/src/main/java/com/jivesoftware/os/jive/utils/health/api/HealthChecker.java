package com.jivesoftware.os.jive.utils.health.api;

import com.jivesoftware.os.jive.utils.health.HealthCheck;

/**
 *
 * @author jonathan.colt
 * @param <C>
 */
public interface HealthChecker<C> extends HealthCheck {

    public void check(C checkable, String description, String resolution);

}
