/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jivesoftware.os.jive.utils.ordered.id;

import com.jivesoftware.os.jive.utils.ordered.id.SnowflakeIdPacker;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SnowflakeIdPackerTest {

    @Test
    public void testContiguousIdsStillHaveRoomForAddAndRemoveDifferentiation() {
        SnowflakeIdPacker snowflakeIdPacker = new SnowflakeIdPacker();
        long id1 = snowflakeIdPacker.pack(1, 1, 1);
        long id2 = snowflakeIdPacker.pack(1, 1, 2);
        Assert.assertTrue(id1 + 1 < id2);
        Assert.assertTrue(id2 - id1 == 2);
    }
}
