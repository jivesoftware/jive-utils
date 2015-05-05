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
package com.jivesoftware.os.jive.utils.http.client.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jivesoftware.os.jive.utils.http.client.HttpClient;
import com.jivesoftware.os.jive.utils.http.client.HttpClientConfiguration;
import com.jivesoftware.os.jive.utils.http.client.HttpClientException;
import com.jivesoftware.os.jive.utils.http.client.HttpClientFactoryProvider;
import com.jivesoftware.os.jive.utils.http.client.HttpResponse;
import com.jivesoftware.os.mlogger.core.MetricLogger;
import com.jivesoftware.os.mlogger.core.MetricLoggerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;

public class SimpleRequestHelper {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();

    private final ObjectMapper mapper;

    private static final byte[] EMPTY_RESPONSE = new byte[0];
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public SimpleRequestHelper() {
        this.mapper = new ObjectMapper();
    }

    /**
     * Sends the request to the server and returns the deserialized results.
     * <p/>
     * If the response body is empty, and the status code is successful, the client returns an empty (but valid) result.
     *
     * @param requestParamsObject request object
     * @param endpointUrl         path to the REST service
     * @param resultClass         type of the result class
     * @param emptyResult         an instance an empty result.
     * @return the result
     * @throws RuntimeException on marshalling, request, or deserialization failure
     */
    public <T> T executeRequest(Object requestParamsObject, String host, int port, String endpointUrl, Class<T> resultClass, T emptyResult) throws IOException {

        String postEntity;
        try {
            postEntity = mapper.writeValueAsString(requestParamsObject);
        } catch (IOException e) {
            throw new RuntimeException("Error serializing request parameters object to a string.  Object " +
                "was " + requestParamsObject, e);
        }

        HttpClient httpClient =
            new HttpClientFactoryProvider().createHttpClientFactory(Collections.<HttpClientConfiguration>emptyList()).createClient(host, port);
        byte[] responseBody = executePostJson(httpClient, endpointUrl, postEntity);

        if (responseBody.length == 0) {
            LOG.warn("Received empty response from http call.  Posted request body was: " + postEntity);
            return emptyResult;
        }

        return mapper.readValue(new ByteArrayInputStream(responseBody), resultClass);
    }

    private byte[] executePostJson(HttpClient httpClient, String endpointUrl, String postEntity) {
        HttpResponse response;
        try {
            response = httpClient.postJson(endpointUrl, postEntity);
        } catch (HttpClientException e) {
            throw new RuntimeException("Error posting query request to server.  The entity posted " +
                "was \"" + postEntity + "\" and the endpoint posted to was \"" + endpointUrl + "\".", e);
        }

        byte[] responseBody = response.getResponseBody();

        if (responseBody == null) {
            responseBody = EMPTY_RESPONSE;
        }

        if (!isSuccessStatusCode(response.getStatusCode())) {
            throw new RuntimeException("Received non success status code (" + response.getStatusCode() + ") " +
                "from the server.  The reason phrase on the response was \"" + response.getStatusReasonPhrase() + "\" " +
                "and the body of the response was \"" + new String(responseBody, UTF_8) + "\".");
        }

        return responseBody;
    }

    private boolean isSuccessStatusCode(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

}
