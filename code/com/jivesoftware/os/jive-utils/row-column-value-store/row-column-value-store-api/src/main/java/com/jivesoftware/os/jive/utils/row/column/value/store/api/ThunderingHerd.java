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
package com.jivesoftware.os.jive.utils.row.column.value.store.api;

import com.jivesoftware.os.jive.utils.base.util.UtilThread;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class provides a method that blocks the calling thread for an amount of time determined by the value of an encapsulated counter. Calling pushback
 * increases blocking time , calling progress decreases it if not already 0.
 *
 * @author jonathan
 */
public class ThunderingHerd {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();
    private final AtomicLong pushback = new AtomicLong(0);

    public ThunderingHerd() {
    }

    /**
     * Increment the counter and the blocking time of herd()
     */
    public void pushback() {
        pushback.incrementAndGet();
    }

    /**
     * We have progress so let the herd run.
     */
    public void progress() {
        pushback.decrementAndGet();
    }

    /**
     * Blocks for an amount of time that is dependent on how positive the encapsulate counter is.
     */
    public void herd() {
        while (true) {
            long at = pushback.get();
            if (at <= 0) {
                pushback.compareAndSet(at, 0);
                break;
            }
            LOG.warn("Slowing the herd! " + pushback);
            LOG.inc("thundering>herd>slowed");
            UtilThread.sleep(Math.min(100 * at, 10000)); // TODO expose and or tune
            if (at >= pushback.get()) { //  is pushback hasn't changed let this thread through
                break;
            }
        }
    }
}
