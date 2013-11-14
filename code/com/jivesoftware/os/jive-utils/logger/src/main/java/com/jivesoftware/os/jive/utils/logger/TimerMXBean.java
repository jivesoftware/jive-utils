package com.jivesoftware.os.jive.utils.logger;

public interface TimerMXBean {

    public double getMin();

    public double getMax();

    public double getMean();

    public double getVariance();

    public double get50ThPercentile();

    public double get75ThPercentile();

    public double get90ThPercentile();

    public double get95ThPercentile();

    public double get999ThPercentile();
}
