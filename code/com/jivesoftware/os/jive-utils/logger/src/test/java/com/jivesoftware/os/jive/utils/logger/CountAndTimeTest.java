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
