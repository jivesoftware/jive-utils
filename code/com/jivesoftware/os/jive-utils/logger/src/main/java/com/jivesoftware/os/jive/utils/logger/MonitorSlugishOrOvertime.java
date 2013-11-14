/*
 * $Revision: 142270 $
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
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
