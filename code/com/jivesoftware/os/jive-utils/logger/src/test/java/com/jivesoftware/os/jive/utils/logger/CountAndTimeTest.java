/*
 * $Revision: 142270 $
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.logger;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author jonathan
 */
public class CountAndTimeTest {

    @Test
    public void testCountAndTime() throws Exception {
        MetricLogger logger = MetricLoggerFactory.getLogger("fooCountAndTimeTest");
        CountAndTime countAndTime = new CountAndTime(logger, "bar");

        countAndTime.call(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(500);
                return "done";
            }
        });

        CountersAndTimers countersAndTimers = CountersAndTimers.getOrCreate("foo");
        Set<Entry<String, Counter>> counters = countersAndTimers.getCounters();
        for (Entry<String, Counter> entry : counters) {
            if (entry.getKey().contains("logged")) {
                continue;
            }
            Assert.assertEquals(entry.getKey(), "bar");
            Assert.assertEquals(entry.getValue().getCount(), 1);
        }
        Set<Entry<String, Timer>> timers = countersAndTimers.getTimers();
        for (Entry<String, Timer> entry : timers) {
            Assert.assertEquals(entry.getKey(), "bar");
            Assert.assertTrue(entry.getValue().getMax() > 250);
        }
    }
}
