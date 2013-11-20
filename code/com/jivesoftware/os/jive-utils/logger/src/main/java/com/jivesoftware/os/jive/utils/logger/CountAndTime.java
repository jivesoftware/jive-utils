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
