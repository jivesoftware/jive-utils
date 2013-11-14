package com.jivesoftware.jive.platform.common.base.service.pojo;

import com.jivesoftware.os.jive.utils.base.service.pojo.Tenancy;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TenancyTest {

    @Test
    public void testTenancyEquality() throws Exception {
        Tenancy tenancy1 = new Tenancy("test", 1234l, 9876l);
        Tenancy tenancy2 = new Tenancy("test", 1234l, 9876l);

        Assert.assertEquals(tenancy1, tenancy2);
    }
}
