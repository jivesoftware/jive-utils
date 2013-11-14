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
