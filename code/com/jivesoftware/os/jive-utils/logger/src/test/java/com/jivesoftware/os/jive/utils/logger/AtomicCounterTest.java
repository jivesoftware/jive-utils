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
