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
