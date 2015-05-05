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
import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;

@Singleton
@Path("/configuration")
public class MainPropertiesEndpoints {

    @Context
    private MainProperties mainProperties;

    @GET
    @Path("/properties")
    public Response mainArgs() {
        try {
            StringBuilder sb = new StringBuilder();
            for (String propertyFile : mainProperties.getPropertiesFiles()) {
                sb.append("//").append(propertyFile);
                sb.append(FileUtils.readFileToString(new File(propertyFile)));
                sb.append("\n\n");
            }
            return Response.ok().entity(sb.toString()).type(MediaType.TEXT_PLAIN).build();
        } catch (IOException x) {
            return ResponseHelper.INSTANCE.errorResponse("Failed to load properties.", x);
        }
    }
}
