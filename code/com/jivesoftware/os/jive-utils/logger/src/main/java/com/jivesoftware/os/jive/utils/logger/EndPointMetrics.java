/*
 * $Revision: 142270 $
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
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
