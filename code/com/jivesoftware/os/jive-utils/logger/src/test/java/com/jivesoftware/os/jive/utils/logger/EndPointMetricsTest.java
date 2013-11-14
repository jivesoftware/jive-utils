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
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author jonathan
 */
public class EndPointMetricsTest {

    @Test
    public void testCountAndTime() throws Exception {

        MetricLogger logger = MetricLoggerFactory.getLogger("fooEndPointMetricsTest");
        EndPointMetrics countAndTime = new EndPointMetrics("bar", logger);

        countAndTime.call(new Callable<String>() {
            @Override
            public String call() throws Exception {
                Thread.sleep(500);
                return "done";
            }
        });

        CountersAndTimers countersAndTimers = CountersAndTimers.getOrCreate("foo");
        Set<Map.Entry<String, Counter>> counters = countersAndTimers.getCounters();
        for (Map.Entry<String, Counter> entry : counters) {
            if (entry.getKey().contains("logged")) {
                continue;
            } else if (entry.getKey().equals("bar>activeThreads")) {
                Assert.assertEquals(entry.getValue().getCount(), 0);
            } else if (entry.getKey().equals("bar>maxThreads")) {
                Assert.assertEquals(entry.getValue().getCount(), 1);
            } else if (entry.getKey().equals("bar>meanThreads")) {
                Assert.assertEquals(entry.getValue().getCount(), 1);
            } else {
                Assert.assertEquals(entry.getKey(), "bar");
                Assert.assertEquals(entry.getValue().getCount(), 1);
            }
        }
        Set<Map.Entry<String, Timer>> timers = countersAndTimers.getTimers();
        for (Map.Entry<String, Timer> entry : timers) {
            Assert.assertEquals(entry.getKey(), "bar");
            Assert.assertTrue(entry.getValue().getMax() > 250);
        }
    }
}
