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
