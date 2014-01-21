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

/**
 * This class provides a method that blocks the calling thread for an amount of time determined by the value of an encapsulated counter. Calling pushback
 * increases blocking time , calling progress decreases it if not already 0.
 *
 * @author jonathan
 */
public class ThunderingHerd {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();
    // deliberately not Atomic
    volatile private long pushback;

    public ThunderingHerd() {
    }

    /**
     * Increment the counter and the blocking time of herd()
     */
    public void pushback() {
        pushback++;
    }

    /**
     * We have progress so let the herd run.
     */
    public void progress() {
        pushback = 0;
    }

    /**
     * Blocks for an amount of time that is dependent on how positive the encapulated counter is.
     */
    public void herd() {
        long localPushback = pushback;
        long lastPushback = localPushback;
        while (localPushback > 0) {
            LOG.warn("Slowing the herd! " + pushback);
            LOG.inc("thundering>herd>slowed");
            // todo expose and or tune
            UtilThread.sleep(Math.min(500 * localPushback, 20000));
            localPushback = pushback;
            if (localPushback < 0) {
                localPushback = 0;
            }
            if (lastPushback >= localPushback) {
                //  is pushback hasn't changed let this thread through
                break;
            }
            lastPushback = localPushback;
        }
    }
}
