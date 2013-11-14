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
public class CounterTest {

    @Test
    public void testIncAndDec() {

        Counter counter = new Counter(ValueType.COUNT);
        Assert.assertEquals(counter.getValueType(), ValueType.COUNT);

        counter.dec();
        Assert.assertEquals(counter.getCount(), -1);

        counter.dec(10);
        Assert.assertEquals(counter.getCount(), -11);

        counter.inc();
        Assert.assertEquals(counter.getCount(), -10);

        counter.inc(5);
        Assert.assertEquals(counter.getCount(), -5);

        counter.reset();
        Assert.assertEquals(counter.getCount(), 0);


        counter.set(100);
        Assert.assertEquals(counter.getValue(), 100);

        Assert.assertEquals(counter.toJsonString(), "{\"type\":\"COUNT\",\"value\":100}");

        counter.setValue(200);
        Assert.assertEquals(counter.getValue(), 200);

        counter.setType(ValueType.RATE);
        Assert.assertEquals(counter.getType(), ValueType.RATE.name());


    }
}
