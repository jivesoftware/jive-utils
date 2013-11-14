/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-2012 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.logger;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Log4jMetricEventWriter implements MetricEventWriter {

    @Override
    public void writeEvent(MetricEvent event) {
        Logger logger = LogManager.getLogger("jive-metrics." + event.getClassName() + "." + event.getMethodName());
        if (logger != null) {
            logger.info(event);
        }
    }
}
