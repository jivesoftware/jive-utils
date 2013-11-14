package com.jivesoftware.os.jive.utils.logger;

public class LazyCounter {

    private final CountersAndTimers countersAndTimers;
    private final ValueType valueType;
    private final String name;
    private Counter counter;

    public LazyCounter(CountersAndTimers countersAndTimers, ValueType valueType, String name) {
        this.countersAndTimers = countersAndTimers;
        this.valueType = valueType;
        this.name = name;
    }

    public void inc() {
        if (counter == null) {
            counter = countersAndTimers.counter(valueType, name);
        }
        counter.inc();
    }
}
