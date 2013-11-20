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
package com.jivesoftware.jive.platform.common.base.service.pojo;

import com.jivesoftware.os.jive.utils.base.service.pojo.TimeRange;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TimeRangeTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidBounds() throws Exception {
        new TimeRange(100l, 1l);
    }

    @Test
    public void testLowerOnlyBound() throws Exception {
        TimeRange timeRange = TimeRange.newLowerBoundOnlyTimeRange(100l);

        Assert.assertTrue(timeRange.hasLowerBound());
        Assert.assertFalse(timeRange.hasUpperBound());
    }

    @Test
    public void testUpperOnlyBound() throws Exception {
        TimeRange timeRange = TimeRange.newUpperBoundOnlyTimeRange(800l);

        Assert.assertTrue(timeRange.hasUpperBound());
        Assert.assertFalse(timeRange.hasLowerBound());
    }

    @Test
    public void testTimeRangeEquality() throws Exception {
        TimeRange timeRange1 = new TimeRange(100l, 4098l);
        TimeRange timeRange2 = new TimeRange(100l, 4098l);

        Assert.assertEquals(timeRange1, timeRange2);
    }
}
