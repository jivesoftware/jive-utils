/*
 * Copyright 2013 Jive Software, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
