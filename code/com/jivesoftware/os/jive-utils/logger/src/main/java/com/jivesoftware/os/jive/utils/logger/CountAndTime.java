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
 *
 * @author jonathan
 */
public class CountAndTime implements CallMonitor {

    final MetricLogger logger;
    final String name;

    public CountAndTime(MetricLogger logger, String name) {
        this.logger = logger;
        this.name = name;
    }

    public void start() {
        logger.inc(name);
        logger.startTimer(name);

    }

    public long stop() {
        return logger.stopTimer(name);
    }

    /**
     * For those who prefer callable over dealing with try finally
     *
     * @param <V>
     * @param callable
     * @return
     * @throws Exception
     */
    @Override
    public <V> V call(Callable<V> callable) throws Exception {
        try {
            start();
            return callable.call();
        } finally {
            stop();
        }
    }
}
