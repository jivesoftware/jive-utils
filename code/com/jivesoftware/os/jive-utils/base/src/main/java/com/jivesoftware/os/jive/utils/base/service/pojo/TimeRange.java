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
package com.jivesoftware.os.jive.utils.base.service.pojo;

/**
 * Defines a range of time
 */
public class TimeRange {

    private static final long BOUND_TIME_NULL_VALUE = 0l;
    public static final TimeRange UNBOUNDED_TIME_RANGE = new TimeRange(BOUND_TIME_NULL_VALUE, BOUND_TIME_NULL_VALUE);
    private final long lowerBoundTimeInMillis;
    private final long upperBoundTimeInMillis;

    /**
     * @param lowerBoundTimeInMillis the lower bound of the time range to create
     * @return a time range object that has only a lower time bound
     */
    public static TimeRange newLowerBoundOnlyTimeRange(long lowerBoundTimeInMillis) {
        return new TimeRange(lowerBoundTimeInMillis, BOUND_TIME_NULL_VALUE);
    }

    /**
     * @param upperBoundTimeInMillis the upper bound of the time range to create
     * @return a time range object that has only an upper time bound
     */
    public static TimeRange newUpperBoundOnlyTimeRange(long upperBoundTimeInMillis) {
        return new TimeRange(BOUND_TIME_NULL_VALUE, upperBoundTimeInMillis);
    }

    public TimeRange(long lowerBoundTimeInMillis, long upperBoundTimeInMillis) {
        if (upperBoundTimeInMillis != BOUND_TIME_NULL_VALUE && lowerBoundTimeInMillis > upperBoundTimeInMillis) {
            throw new IllegalArgumentException("Lower bound cannot be greater than upper bound!");
        }
        this.lowerBoundTimeInMillis = lowerBoundTimeInMillis;
        this.upperBoundTimeInMillis = upperBoundTimeInMillis;
    }

    public long getLowerBoundTimeInMillis() {
        return lowerBoundTimeInMillis;
    }

    public long getUpperBoundTimeInMillis() {
        return upperBoundTimeInMillis;
    }

    public boolean hasLowerBound() {
        return lowerBoundTimeInMillis != BOUND_TIME_NULL_VALUE;
    }

    public boolean hasUpperBound() {
        return upperBoundTimeInMillis != BOUND_TIME_NULL_VALUE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TimeRange)) {
            return false;
        }

        TimeRange timeRange = (TimeRange) o;

        if (lowerBoundTimeInMillis != timeRange.lowerBoundTimeInMillis) {
            return false;
        }
        if (upperBoundTimeInMillis != timeRange.upperBoundTimeInMillis) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (lowerBoundTimeInMillis ^ (lowerBoundTimeInMillis >>> 32));
        result = 31 * result + (int) (upperBoundTimeInMillis ^ (upperBoundTimeInMillis >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "TimeRange{"
                + "lowerBoundTimeInMillis=" + lowerBoundTimeInMillis
                + ", upperBoundTimeInMillis=" + upperBoundTimeInMillis
                + '}';
    }
}
