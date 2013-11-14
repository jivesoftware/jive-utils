/*
 * $Revision: 142270 $
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.logger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author jonathan
 */
public class MonitorSlugishOrOvertimeTest {

    @Test
    public void testCountAndTime() throws Exception {
        MetricLogger logger = MetricLoggerFactory.getLogger("fooMonitorSlugishOrOvertimeTest");

        long sluggishWhenMoreThanN = 100;
        long overtimeWhenMoreThanN = 200;

        MonitorSlugishOrOvertime monitorSlugishOrOvertime = new MonitorSlugishOrOvertime(logger, "bar", TimeUnit.MILLISECONDS,
                sluggishWhenMoreThanN, overtimeWhenMoreThanN);

        monitorSlugishOrOvertime.call(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(10);
                return "done";
            }
        });

        CountersAndTimers countersAndTimers = CountersAndTimers.getOrCreate("foo");
        Set<Map.Entry<String, Counter>> counters = countersAndTimers.getCounters();
        for (Map.Entry<String, Counter> entry : counters) {
            if (entry.getKey().contains("logged")) {
                continue;
            }
            Assert.assertEquals(entry.getKey(), "bar");
            Assert.assertEquals(entry.getValue().getCount(), 1);
        }

        monitorSlugishOrOvertime.call(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(110);
                return "done";
            }
        });

        counters = countersAndTimers.getCounters();
        for (Map.Entry<String, Counter> entry : counters) {
            if (entry.getKey().contains("logged")) {
                continue;
            }
            if (entry.getKey().contains("bar>slugish")) {
                Assert.assertEquals(entry.getValue().getCount(), 1);
                continue;
            }
            Assert.assertEquals(entry.getKey(), "bar");
            Assert.assertEquals(entry.getValue().getCount(), 2);
        }


        monitorSlugishOrOvertime.call(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(210);
                return "done";
            }
        });

        counters = countersAndTimers.getCounters();
        for (Map.Entry<String, Counter> entry : counters) {
            if (entry.getKey().contains("logged")) {
                continue;
            }
            if (entry.getKey().contains("bar>slugish")) {
                Assert.assertEquals(entry.getValue().getCount(), 1);
                continue;
            }
            if (entry.getKey().contains("bar>overtime")) {
                Assert.assertEquals(entry.getValue().getCount(), 1);
                continue;
            }
            Assert.assertEquals(entry.getKey(), "bar");
            Assert.assertEquals(entry.getValue().getCount(), 3);
        }

    }
}
