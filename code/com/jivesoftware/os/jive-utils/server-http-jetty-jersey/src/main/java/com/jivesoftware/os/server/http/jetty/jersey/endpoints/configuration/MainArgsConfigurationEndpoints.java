package com.jivesoftware.os.server.http.jetty.jersey.endpoints.configuration;

import com.google.inject.Singleton;
import com.jivesoftware.os.jive.utils.jaxrs.util.ResponseHelper;
import java.io.File;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Singleton
@Path("/configuration")
public class MainArgsConfigurationEndpoints {

    @Context
    private MainArgsConfigurationFile configFile;

    @GET
    @Path("/main-args")
    public Response isEligible(@QueryParam("callback") @DefaultValue("") String callback) {
        if (callback.length() > 0) {
            return ResponseHelper.INSTANCE.jsonpResponse(callback, new File(configFile.getConfigFile()));
        } else {
            return ResponseHelper.INSTANCE.jsonResponse(new File(configFile.getConfigFile()));
        }
    }

}
