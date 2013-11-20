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
package com.jivesoftware.jive.deployer.jaxrs.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jivesoftware.os.jive.utils.jaxrs.util.ResponseHelper;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.ws.rs.core.Response;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ResponseHelperTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testErrorResponse() throws Exception {
        ObjectNode jsonNode = objectMapper.createObjectNode();
        jsonNode.put("Foobar", true);
        String message = "This is a BAD request!";
        Exception e = new Exception();
        OutputStream outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        e.printStackTrace(printStream);
        Response response = ResponseHelper.INSTANCE.errorResponse(Response.Status.BAD_REQUEST, message, e, jsonNode);
        assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
        String bodyString = objectMapper.writeValueAsString(response.getEntity());
        System.out.println(bodyString);
        ObjectNode body = (ObjectNode) objectMapper.readTree(bodyString);
        assertEquals(body.get("message").textValue(), message);
        assertEquals(body.get("trace").textValue(), outputStream.toString());
        assertEquals(body.get("relatedData"), jsonNode);
    }
}
