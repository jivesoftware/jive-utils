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
package com.jivesoftware.jive.platform.common.base.util;

import com.jivesoftware.os.jive.utils.base.util.LockProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LockProviderTest {

    @Test
    public void testLockSameForSameKey() throws Exception {
        LockProvider<String> lockProvider = new LockProvider<>();

        Object lock1 = lockProvider.getLock("testKey");
        Object lock2 = lockProvider.getLock("testKey");

        Assert.assertSame(lock1, lock2);
    }
}
