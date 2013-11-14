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
