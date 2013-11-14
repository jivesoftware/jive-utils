/*
 * $Revision: 142270 $
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.logger;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author jonathan
 */
public class RunningThreadTracker {

    static final private MetricLogger defaultLogger = MetricLoggerFactory.getLogger();
    private final Set<Thread> runningThread = Collections.newSetFromMap(new ConcurrentHashMap<Thread, Boolean>());
    private final String name;
    private final MinMaxInt sample = new MinMaxInt();
    private final MetricLogger logger;

    public RunningThreadTracker(String name) {
        this(defaultLogger, name);
    }

    public RunningThreadTracker(MetricLogger logger, String name) {
        this.logger = logger;
        this.name = name;
    }

    /**
     * Call MUST be diligent about calling exit
     */
    public void enter() {
        Thread currentThread = Thread.currentThread();
        runningThread.add(currentThread);
        int size = runningThread.size();
        sample.value(size);
        logger.set(ValueType.VALUE, name + ">activeThreads", size);
        logger.set(ValueType.VALUE, name + ">maxThreads", sample.max());
        logger.set(ValueType.VALUE, name + ">meanThreads", (int) sample.mean());
    }

    public void exit() {
        runningThread.remove(Thread.currentThread());
        logger.set(ValueType.VALUE, name + ">activeThreads", runningThread.size());
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
            enter();
            return callable.call();
        } finally {
            exit();
        }
    }
}
