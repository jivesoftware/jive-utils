/*
 * Copyright 2014 Jive Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jivesoftware.os.jive.utils.http.client.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jivesoftware.os.jive.utils.http.client.HttpClient;
import com.jivesoftware.os.jive.utils.http.client.HttpClientException;
import com.jivesoftware.os.jive.utils.http.client.HttpResponse;
import com.jivesoftware.os.mlogger.core.MetricLogger;
import com.jivesoftware.os.mlogger.core.MetricLoggerFactory;
import java.io.IOException;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 *
 * @author jonathan.colt
 */
public class JsonRequestBuilder {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();

    private final String url;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private Map<String, String> headers = null;
    private static final byte[] EMPTY_RESPONSE = new byte[0];

    JsonRequestBuilder(String url, HttpClient httpClient, ObjectMapper mapper) {
        this.url = url;
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    public JsonRequestBuilder setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public <T> T post(Object requestParamsObject, Class<T> resultClass, T emptyResult) {
        String postEntity;
        try {
            postEntity = mapper.writeValueAsString(requestParamsObject);
        } catch (IOException e) {
            throw new RuntimeException("Error serializing request parameters object to a string.  Object "
                + "was " + requestParamsObject, e);
        }

        byte[] responseBody = executePostJson(httpClient, url, postEntity);

        if (responseBody.length == 0) {
            LOG.warn("Received empty response from http call.  Posted request body was: " + postEntity);
            return emptyResult;
        }

        return extractResultFromResponse(responseBody, resultClass);
    }

    private byte[] executePostJson(HttpClient httpClient, String endpointUrl, String postEntity) {
        HttpResponse response;
        try {
            response = httpClient.postJson(endpointUrl, postEntity, headers);
        } catch (HttpClientException e) {
            throw new RuntimeException("Error posting query request to server.  The entity posted "
                + "was \"" + postEntity + "\" and the endpoint posted to was \"" + endpointUrl + "\".", e);
        }

        byte[] responseBody = response.getResponseBody();

        if (responseBody == null) {
            responseBody = EMPTY_RESPONSE;
        }

        if (!isSuccessStatusCode(response.getStatusCode())) {
            throw new RuntimeException("Received non success status code (" + response.getStatusCode() + ") "
                + "from the server.  The reason phrase on the response was \"" + response.getStatusReasonPhrase() + "\" "
                + "and the body of the response was \"" + new String(responseBody, UTF_8) + "\".");
        }

        return responseBody;
    }

    private boolean isSuccessStatusCode(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    private <T> T extractResultFromResponse(byte[] responseBody, Class<T> resultClass) {
        T result;
        try {
            result = mapper.readValue(responseBody, 0, responseBody.length, resultClass);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing response body into result "
                + "object.  Response body was \"" + (responseBody != null ? new String(responseBody, UTF_8) : "null")
                + "\".", e);
        }

        return result;
    }
}
