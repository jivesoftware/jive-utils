package com.jivesoftware.os.jive.utils.health.api;

import com.jivesoftware.os.jive.utils.logger.Counter;
import com.jivesoftware.os.jive.utils.logger.CountersAndTimers;
import com.jivesoftware.os.jive.utils.logger.ValueType;

/**
 *
 * @author jonathan.colt
 */
public class HealthCounter {

    private final CountersAndTimers countersAndTimers;
    private final String name;
    private final HealthChecker<Counter> healthChecker;

    public HealthCounter(CountersAndTimers countersAndTimers, String name, HealthChecker<Counter> healthChecker) {
        this.countersAndTimers = countersAndTimers;
        this.name = name;
        this.healthChecker = healthChecker;
    }

    public void inc(String unhealthyMessage) {
        inc(1, unhealthyMessage);
    }

    public void inc(long amount, String context) {
        Counter counter = countersAndTimers.counter(ValueType.COUNT, name);
        counter.inc(amount);
        healthChecker.check(counter, context);
    }

    public void set(long value, String context) {
        Counter counter = countersAndTimers.counter(ValueType.COUNT, name);
        counter.set(value);
        healthChecker.check(counter, context);
    }


}
