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
public class AtomicCounterTest {

    @Test
    public void testIncAndDec() {

        AtomicCounter atomicCounter = new AtomicCounter(ValueType.COUNT);
        Assert.assertEquals(atomicCounter.getValueType(), ValueType.COUNT);

        atomicCounter.dec();
        Assert.assertEquals(atomicCounter.getCount(), -1);

        atomicCounter.dec(10);
        Assert.assertEquals(atomicCounter.getCount(), -11);

        atomicCounter.inc();
        Assert.assertEquals(atomicCounter.getCount(), -10);

        atomicCounter.inc(5);
        Assert.assertEquals(atomicCounter.getCount(), -5);

        atomicCounter.reset();
        Assert.assertEquals(atomicCounter.getCount(), 0);


        atomicCounter.set(100);
        Assert.assertEquals(atomicCounter.getValue(), 100);

        Assert.assertEquals(atomicCounter.toJsonString(), "{\"type\":\"COUNT\",\"value\":100}");

        atomicCounter.setValue(200);
        Assert.assertEquals(atomicCounter.getValue(), 200);

        atomicCounter.setType(ValueType.RATE);
        Assert.assertEquals(atomicCounter.getType(), ValueType.RATE.name());
    }
}
