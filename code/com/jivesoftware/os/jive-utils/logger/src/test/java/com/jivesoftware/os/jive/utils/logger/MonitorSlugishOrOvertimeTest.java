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
