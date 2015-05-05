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
package com.jivesoftware.os.server.http.jetty.jersey.endpoints.killswitch;

import java.util.concurrent.atomic.AtomicBoolean;
import org.testng.Assert;
import org.testng.annotations.Test;

public class KillSwitchTest {

    @Test
    public void testGets() {
        KillSwitch instance = new KillSwitch("hi", new AtomicBoolean(true));
        Assert.assertEquals(instance.getName(), "hi");
        Assert.assertEquals(instance.getState().get(), true);
    }

    @Test
    public void hardenToString() {
        KillSwitch instanceA = new KillSwitch("hi", new AtomicBoolean(true));
        System.out.println("KillSwitch toString=" + instanceA.toString());
        Assert.assertEquals(instanceA.toString(), "KillSwitch{name=hi, state=true}");
    }
}
