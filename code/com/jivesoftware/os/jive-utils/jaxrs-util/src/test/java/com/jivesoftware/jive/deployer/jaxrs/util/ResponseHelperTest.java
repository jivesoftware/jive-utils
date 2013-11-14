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
