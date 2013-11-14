package com.jivesoftware.os.server.http.jetty.jersey.endpoints.killswitch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.ws.rs.core.Response;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class KillSwitchsRestEndpointsTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private KillSwitchsRestEndpoints instance;
    private KillSwitchService killSwitchService;

    @BeforeMethod
    public void setUpMethod() throws Exception {
        killSwitchService = Mockito.mock(KillSwitchService.class);
        instance = new KillSwitchsRestEndpoints(killSwitchService);
    }

    @Test
    public void testListKillSwitches() throws IOException {
        Mockito.when(killSwitchService.getAll()).thenReturn(new ArrayList<KillSwitch>());
        Response result = instance.listKillSwitches();
        ArrayNode array = mapper.readValue(result.getEntity().toString(), ArrayNode.class);
        Assert.assertTrue(array.size() == 0);

        Mockito.when(killSwitchService.getAll()).thenReturn(Arrays.asList(new KillSwitch("hi", new AtomicBoolean(true))));
        result = instance.listKillSwitches();
        array = mapper.readValue(result.getEntity().toString(), ArrayNode.class);
        Assert.assertTrue(array.size() == 1);

    }

    @Test
    public void testSetKillSwitch() throws IOException {
        Mockito.when(killSwitchService.set("hi", true)).thenReturn(true);
        Response result = instance.setKillSwitch("hi", true);
        ObjectNode got = mapper.readValue(result.getEntity().toString(), ObjectNode.class);
        Assert.assertTrue(got.get("state").booleanValue());

        Mockito.when(killSwitchService.set("hi", false)).thenReturn(true);
        result = instance.setKillSwitch("hi", false);
        got = mapper.readValue(result.getEntity().toString(), ObjectNode.class);
        Assert.assertFalse(got.get("state").booleanValue());
    }
}
