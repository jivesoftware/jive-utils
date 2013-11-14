package com.jivesoftware.os.server.http.jetty.jersey.endpoints.killswitch;

import com.jivesoftware.os.jive.utils.jaxrs.util.ResponseHelper;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("/killswitch")
public class KillSwitchsRestEndpoints {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();
    private final KillSwitchService killSwitchService;

    public KillSwitchsRestEndpoints(@Context KillSwitchService killSwitchService) {
        this.killSwitchService = killSwitchService;
    }

    @GET
    @Path("/list")
    public Response listKillSwitches() {
        return ResponseHelper.INSTANCE.jsonResponse(killSwitchService.getAll());
    }

    @GET
    @Path("/set")
    public Response setKillSwitch(
        @QueryParam("name") @DefaultValue("") String name,
        @QueryParam("state") @DefaultValue("false") boolean state) {
        boolean set = killSwitchService.set(name, state);
        if (set) {
            KillSwitch killSwitch = new KillSwitch(name, new AtomicBoolean(state));
            LOG.info("Set " + killSwitch);
            return ResponseHelper.INSTANCE.jsonResponse(killSwitch);
        } else {
            LOG.warn("Failed to set KillSwitch name:" + name + " to state:" + state);
            return ResponseHelper.INSTANCE.errorResponse("Failed to set killswitch:" + name
                + " to " + state + ". This is like because the switch doesn't exist.");
        }
    }
}
