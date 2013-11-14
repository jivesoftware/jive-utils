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
