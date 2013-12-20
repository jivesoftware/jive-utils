/*
 * $Revision: 142270 $
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.row.column.value.store.shared;

import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;


/**
 *
 * @author jonathan
 */
public class RowColumnValueStoreCounters {

    final private static MetricLogger logger = MetricLoggerFactory.getLogger();
    private final String storeName;

    public RowColumnValueStoreCounters(String storeName) {
        this.storeName = storeName;
    }

    public void added(int count) {
        logger.inc("add", count);
        logger.inc(storeName + ">add", count);
    }

    public void got(int count) {
        logger.inc("get", count);
        logger.inc(storeName + ">get", count);
    }

    public void removed(int count) {
        logger.inc("removed", count);
        logger.inc(storeName + ">removed", count);
    }

    public void sliced(int count) {
        logger.inc("sliced", count);
        logger.inc(storeName + ">sliced", count);
    }

    public void timedout() {
        logger.inc("timeout");
        logger.inc(storeName + ">timeout");
    }

    public void notfound() {
        logger.inc("notfound");
        logger.inc(storeName + ">notfound");
    }

    public void unavailable() {
        logger.inc("unavailable");
        logger.inc(storeName + ">unavailable");
    }
}
