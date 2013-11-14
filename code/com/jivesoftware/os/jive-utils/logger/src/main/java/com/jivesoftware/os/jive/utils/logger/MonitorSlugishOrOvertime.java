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

import java.util.concurrent.TimeUnit;

/**
 *
 * @author jonathan
 */
public class MonitorSlugishOrOvertime extends CountAndTime {

    private final TimeUnit timeUnit;
    private final long sluggishWhenMoreThanN;
    private final long overtimeWhenMoreThanN;

    public MonitorSlugishOrOvertime(MetricLogger logger, String name, TimeUnit timeUnit, long sluggishWhenMoreThanN,
            long overtimeWhenMoreThanN) {
        super(logger, name);
        this.timeUnit = timeUnit;
        this.sluggishWhenMoreThanN = sluggishWhenMoreThanN;
        this.overtimeWhenMoreThanN = overtimeWhenMoreThanN;

    }

    @Override
    public long stop() {
        long elapse = super.stop();
        if (elapse > timeUnit.toMillis(overtimeWhenMoreThanN)) {
            logger.error(name + " is overtime!");
            logger.inc(name + ">overtime");
        } else if (elapse > timeUnit.toMillis(sluggishWhenMoreThanN)) {
            logger.warn(name + " is sluggish!");
            logger.inc(name + ">slugish");
        }

        return elapse;
    }
}
