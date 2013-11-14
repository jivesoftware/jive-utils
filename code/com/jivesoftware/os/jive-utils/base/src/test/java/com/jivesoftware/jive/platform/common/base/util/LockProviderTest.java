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
