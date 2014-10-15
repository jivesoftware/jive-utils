package com.jivesoftware.os.jive.utils.health.api;

import org.merlin.config.defaults.LongDefault;

/**
 *
 * @author jonathan.colt
 */
public interface ScheduledMinMaxHealthCheckConfig extends MinMaxHealthCheckConfig {

    @LongDefault (1000 * 60 * 5)
    Long getCheckIntervalInMillis();

}
