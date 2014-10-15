package com.jivesoftware.os.jive.utils.health.api;

import org.merlin.config.defaults.DoubleDefault;
import org.merlin.config.defaults.IntDefault;

/**
 *
 * @author jonathan.colt
 */
public interface TimerHealthCheckConfig extends HealthCheckConfig {

    @IntDefault(500)
    Integer getSampleWindowSize();

    @DoubleDefault(Double.MAX_VALUE)
    Double getMeanMax();

    @DoubleDefault(Double.MAX_VALUE)
    Double getVarianceMax();

    @DoubleDefault(Double.MAX_VALUE)
    Double get50ThPecentileMax();

    @DoubleDefault(Double.MAX_VALUE)
    Double get75ThPecentileMax();

    @DoubleDefault(Double.MAX_VALUE)
    Double get90ThPecentileMax();

    @DoubleDefault(Double.MAX_VALUE)
    Double get95ThPecentileMax();

    @DoubleDefault(Double.MAX_VALUE)
    Double get99ThPecentileMax();

}
