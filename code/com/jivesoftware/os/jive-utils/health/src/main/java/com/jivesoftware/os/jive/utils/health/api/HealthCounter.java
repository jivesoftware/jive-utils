package com.jivesoftware.os.jive.utils.health.api;

import com.jivesoftware.os.mlogger.core.Counter;
import com.jivesoftware.os.mlogger.core.CountersAndTimers;
import com.jivesoftware.os.mlogger.core.ValueType;

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

    public void inc(String description, String resolution) {
        inc(1, description, resolution);
    }

    public void inc(long amount, String description, String resolution) {
        Counter counter = countersAndTimers.counter(ValueType.COUNT, name);
        counter.inc(amount);
        healthChecker.check(counter, description, resolution);
    }

    public void dec(String description, String resolution) {
        dec(1, description, resolution);
    }

    public void dec(long amount, String description, String resolution) {
        Counter counter = countersAndTimers.counter(ValueType.COUNT, name);
        counter.dec(amount);
        healthChecker.check(counter, description, resolution);
    }

    public void set(long value, String description, String resolution) {
        Counter counter = countersAndTimers.counter(ValueType.COUNT, name);
        counter.set(value);
        healthChecker.check(counter, description, resolution);
    }


}
