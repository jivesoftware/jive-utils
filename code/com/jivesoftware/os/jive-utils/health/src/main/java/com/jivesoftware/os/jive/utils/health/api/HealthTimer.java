package com.jivesoftware.os.jive.utils.health.api;

import com.jivesoftware.os.jive.utils.logger.CountersAndTimers;
import com.jivesoftware.os.jive.utils.logger.Timer;

/**
 *
 * @author jonathan.colt
 */
public class HealthTimer {

    private final CountersAndTimers countersAndTimers;
    private final String name;
    private final HealthChecker<Timer> healthChecker;

    public HealthTimer(CountersAndTimers countersAndTimers, String name, HealthChecker<Timer> healthChecker) {
        this.countersAndTimers = countersAndTimers;
        this.name = name;
        this.healthChecker = healthChecker;
    }

    public void startTimer() {
        countersAndTimers.startTimer(name);
    }

    public long stopTimer(String context) {
        Timer timer = countersAndTimers.stopAndGetTimer(name, name, 5000);
        healthChecker.check(timer, context);
        return timer.getLastSample();
    }

}
