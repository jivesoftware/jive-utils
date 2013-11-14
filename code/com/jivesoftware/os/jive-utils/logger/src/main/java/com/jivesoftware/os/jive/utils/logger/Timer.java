/*
 * $Revision: 142270 $
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.logger;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author jonathan
 */
public class Timer implements TimerMXBean {

    DescriptiveStatistics stats = new DescriptiveStatistics(100);

    public Timer() {
    }

    public void sample(long elapse) {
        stats.addValue(elapse);
    }

    public void reset() {
        stats.clear();
    }

    @Override
    public double getMin() {
        return stats.getMin();
    }

    @Override
    public double getMax() {
        return stats.getMax();
    }

    @Override
    public double getMean() {
        return stats.getMean();
    }

    @Override
    public double getVariance() {
        return stats.getVariance();
    }

    @Override
    public double get50ThPercentile() {
        return stats.getPercentile(50);
    }

    @Override
    public double get75ThPercentile() {
        return stats.getPercentile(50);
    }

    @Override
    public double get90ThPercentile() {
        return stats.getPercentile(90);
    }

    @Override
    public double get95ThPercentile() {
        return stats.getPercentile(95);
    }

    @Override
    public double get999ThPercentile() {
        return stats.getPercentile(99.9);
    }
}
