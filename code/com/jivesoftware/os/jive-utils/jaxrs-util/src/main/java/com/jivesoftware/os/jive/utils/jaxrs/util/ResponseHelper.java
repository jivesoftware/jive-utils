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
package com.jivesoftware.os.jive.utils.jaxrs.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/** Things we find ourselves doing over and over again with jetty. */
public class ResponseHelper {

    public static ResponseHelper INSTANCE = new ResponseHelper();

    private ResponseHelper() {
    }

    private final static MetricLogger log = MetricLoggerFactory.getLogger();
    private final static ObjectMapper jsonMapper = new ObjectMapper();

    static {
        jsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    public ResponseHelper register(Module module) {
        jsonMapper.registerModule(module);
        return this;
    }

    public Response jsonResponse(File jsonFile) {
        String jsonString;
        try {
            jsonString = jsonMapper.writeValueAsString(jsonMapper.readValue(jsonFile, ObjectNode.class));
        } catch (Exception x) {
            log.error("failed to marshall object to jsonString. object=" + jsonFile, x);
            return errorResponse("server failed to jsonFile result object to jsonString.", x);
        }
        return Response.ok().entity(jsonString).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    public Response jsonpResponse(String callbackName, File jsonFile) {
        String jsonString;
        try {
            jsonString = jsonMapper.writeValueAsString(jsonMapper.readValue(jsonFile, ObjectNode.class));
            jsonString = callbackName + "(" + jsonString + ");";
        } catch (Exception x) {
            log.error("failed to marshall object to jsonString. object=" + jsonFile, x);
            return errorResponse("server failed to marshall result object to jsonString.", x);
        }
        return Response.ok().entity(jsonString).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    /** Turns a jsonable object into a jetty web service response. */
    public Response jsonResponse(Object jsonableObject) {
        String jsonString;
        try {
            jsonString = jsonMapper.writeValueAsString(jsonableObject);
        } catch (Exception x) {
            log.error("failed to marshall object to jsonString. object=" + jsonableObject, x);
            return errorResponse("server failed to marshall result object to jsonString.", x);
        }
        return Response.ok().entity(jsonString).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    public Response jsonpResponse(String callbackName, Object jsonableObject) {
        String jsonString;
        try {
            jsonString = jsonMapper.writeValueAsString(jsonableObject);
            jsonString = callbackName + "(" + jsonString + ");";
        } catch (Exception x) {
            log.error("failed to marshall object to jsonString. object=" + jsonableObject, x);
            return errorResponse("server failed to marshall result object to jsonString.", x);
        }
        return Response.ok().entity(jsonString).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    public Response errorResponse(Response.Status status, String message) {
        return errorResponse(status, message, null, null);
    }

    public Response errorResponse(Response.Status status, String message, JsonNode relatedData) {
        return errorResponse(status, message, null, relatedData);
    }

    public Response errorResponse(Response.Status status, String message, Exception e) {
        return errorResponse(status, message, e, null);
    }

    public Response errorResponse(Response.Status status, String message, Exception e, JsonNode relatedData) {
        String trace = null;
        if (e != null) {
            StringWriter writer = new StringWriter();
            try (PrintWriter printWriter = new PrintWriter(writer)) {
                e.printStackTrace(printWriter);
                printWriter.flush();
            }
            trace = writer.toString();
        }
        ErrorResponse errorResponse = new ErrorResponse(message, trace, relatedData);
        return Response.status(status).type(MediaType.APPLICATION_JSON).entity(errorResponse).build();
    }

    /**
     *
     * @param message
     * @param e
     * @return
     */
    public Response errorResponse(String message, Exception e) {
        return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, message, e);
    }

    public Response errorResponse(String message) {
        return errorResponse(Response.Status.INTERNAL_SERVER_ERROR, message);
    }
}
