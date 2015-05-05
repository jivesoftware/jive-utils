package com.jivesoftware.os.jive.utils.health.api;

import org.merlin.config.defaults.LongDefault;

/**
 *
 * @author jonathan.colt
 */
public interface MinMaxHealthCheckConfig extends HealthCheckConfig {

    @LongDefault(0)
    Long getMin();

    @LongDefault(Long.MAX_VALUE)
    Long getMax();

}
