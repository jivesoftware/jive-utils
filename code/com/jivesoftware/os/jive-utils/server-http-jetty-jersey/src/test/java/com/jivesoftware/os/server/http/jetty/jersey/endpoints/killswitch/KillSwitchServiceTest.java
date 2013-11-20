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

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class KillSwitchServiceTest {

    private KillSwitchService instance;

    @BeforeMethod
    public void setUpMethod() throws Exception {
        instance = new KillSwitchService();
    }

    @Test
    public void testAddNull() {
        Assert.assertNull(instance.add(null));
    }

    @Test
    public void testAdd() {
        KillSwitch added = instance.add(new KillSwitch("hi", new AtomicBoolean()));
        KillSwitch addedAgain = instance.add(new KillSwitch("hi", new AtomicBoolean()));
        Assert.assertEquals(addedAgain, added);
    }

    @Test
    public void testIs() {
        AtomicBoolean state = new AtomicBoolean(true);
        KillSwitch added = instance.add(new KillSwitch("hi", state));
        Assert.assertTrue(added.getState().get());
        state.set(false);
        Assert.assertFalse(added.getState().get());
    }

    @Test
    public void testSet() {
        AtomicBoolean state = new AtomicBoolean(true);
        KillSwitch added = instance.add(new KillSwitch("hi", state));
        Assert.assertTrue(added.getState().get());
        instance.set("hi", false);
        Assert.assertFalse(added.getState().get());
        Assert.assertFalse(state.get());
    }

    @Test
    public void testGetAll() {
        instance.add(new KillSwitch("hi", new AtomicBoolean()));
        instance.add(new KillSwitch("hi", new AtomicBoolean()));
        instance.add(new KillSwitch("hello", new AtomicBoolean()));
        instance.add(new KillSwitch("yo", new AtomicBoolean()));
        Collection<KillSwitch> all = instance.getAll();
        Assert.assertTrue(all.size() == 3);
    }
}
