/*
 * $Revision: 142270 $
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.logger;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author jonathan
 */
public class TimerTest {

    @Test
    public void testIncAndDec() {
        Timer timer = new Timer();

        timer.sample(100);
        timer.sample(200);
        timer.sample(300);


        Assert.assertEquals(timer.getMin(), 100d, 0.01d);
        Assert.assertEquals(timer.getMax(), 300d, 0.01d);
        Assert.assertEquals(timer.getMean(), 200d, 0.01d);

        timer.reset();

        Assert.assertTrue(Double.isNaN(timer.getMin()));
        Assert.assertTrue(Double.isNaN(timer.getMax()));
        Assert.assertTrue(Double.isNaN(timer.getMean()));

    }

    // Testing timing, always a flaky thing to do, but this is a sanity check
    @Test
    public void testStartStop() throws InterruptedException {
        int sleep1 = 200, sleep2 = 800;
        MetricLogger log = new MetricLogger("foo", LoggerSummary.INSTANCE);

        log.startTimer("timer1");
        Thread.sleep(sleep1);
        log.stopTimer("timer1");

        log.startTimer("timer1");
        Thread.sleep(sleep2);
        log.stopTimer("timer1");

        Timer timer1 = log.countersAndTimers.getTimerIfAvailable("timer1");
        Assert.assertTrue(timer1.getMean() > 0.48 * (sleep1 + sleep2));
        Assert.assertTrue(timer1.getMean() < 0.52 * (sleep1 + sleep2));
        Assert.assertEquals(2, timer1.stats.getN());

    }
}
