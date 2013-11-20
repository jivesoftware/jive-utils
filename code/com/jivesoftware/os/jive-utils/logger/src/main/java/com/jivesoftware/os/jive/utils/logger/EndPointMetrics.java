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

import java.util.concurrent.Callable;

/**
 * Simple one stop shopping to gather metrics around a service endpoint
 */
public class EndPointMetrics {

    private static final MetricLogger log = MetricLoggerFactory.getLogger();
    private final RunningThreadTracker runningThreadTracker;
    private final CountAndTime countAndTime;

    /**
     *
     * @param name cannot be null.
     */
    public EndPointMetrics(String name) {
        this(name, log);
    }

    /**
     *
     * @param name cannot be null.
     * @param log cannot be null.
     */
    public EndPointMetrics(String name, MetricLogger log) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null.");
        }
        if (log == null) {
            throw new IllegalArgumentException("logger cannot be null.");
        }
        this.runningThreadTracker = new RunningThreadTracker(log, name);
        this.countAndTime = new CountAndTime(log, name);
    }

    public void start() {
        runningThreadTracker.enter();
        countAndTime.start();
    }

    public void stop() {
        runningThreadTracker.exit();
        countAndTime.stop();
    }

    /**
     * For those who prefer callable over dealing with try finally
     *
     * @param <V>
     * @param callable
     * @return
     * @throws Exception
     */
    public <V> V call(Callable<V> callable) throws Exception {
        try {
            start();
            return callable.call();
        } finally {
            stop();
        }
    }
}
