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
