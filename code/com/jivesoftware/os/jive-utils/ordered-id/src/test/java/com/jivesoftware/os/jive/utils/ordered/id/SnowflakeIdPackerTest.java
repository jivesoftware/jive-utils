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
package com.jivesoftware.os.jive.utils.ordered.id;

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