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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jivesoftware.os.jive.utils.http.client.HttpClient;
import com.jivesoftware.os.jive.utils.http.client.HttpClientException;
import com.jivesoftware.os.jive.utils.http.client.HttpResponse;
import com.jivesoftware.os.jive.utils.http.client.HttpStreamResponse;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import java.io.IOException;
import java.nio.charset.Charset;

public class RequestHelper {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger(RequestHelper.class);
    public static final int DEFAULT_STREAM_TIMEOUT_MILLIS = 5000;

    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    private static final byte[] EMPTY_RESPONSE = new byte[0];
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public RequestHelper(HttpClient httpClient, ObjectMapper mapper) {
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    public JsonRequestBuilder create(String url) {
        return new JsonRequestBuilder(url, httpClient, mapper);
    }

    /**
     * Sends the request to the server and returns the deserialized results.
     * <p/>
     * If the response body is empty, and the status code is successful, the client returns an empty (but valid) result.
     *
     * @param endpointUrl path to the REST service
     * @param resultClass type of the result class
     * @param emptyResult an instance an empty result.
     * @return the result
     * @throws RuntimeException on marshalling, request, or deserialization failure
     */
    public <T> T executeGetRequest(String endpointUrl, Class<T> resultClass, T emptyResult) {

        byte[] responseBody = executeGetJson(httpClient, endpointUrl);

        if (responseBody.length == 0) {
            LOG.warn("Received empty response from http call. The endpoint posted to was " + endpointUrl + "\".");
            return emptyResult;
        }

        return extractResultFromResponse(responseBody, resultClass);
    }

    /**
     * Sends the request to the server and returns the streames results.
     * <p/>
     * If the response body is empty, and the status code is successful, the client returns an empty (but valid) result.
     *
     * @param endpointUrl path to the REST service
     * @return the result
     * @throws RuntimeException on marshalling, request, or deserialization failure
     */
    public HttpStreamResponse executeGetStream(String endpointUrl) {

        HttpStreamResponse ret = executeGetStream(httpClient, endpointUrl);

        if (ret == null) {
            LOG.warn("Received empty response from http call. The endpoint posted to was " + endpointUrl + "\".");
            return null;
        }

        return ret;
    }

    /**
     * Sends the request to the server and returns the deserialized results.
     * <p/>
     * If the response body is empty, and the status code is successful, the client returns an empty (but valid) result.
     *
     * @param endpointUrl path to the REST service
     * @param resultClass type of the result class
     * @param emptyResult an instance an empty result.
     * @return the result
     * @throws RuntimeException on marshalling, request, or deserialization failure
     */
    public <T> T executeDeleteRequest(String endpointUrl, Class<T> resultClass, T emptyResult) {

        byte[] responseBody = executeDeleteJson(httpClient, endpointUrl);

        if (responseBody.length == 0) {
            LOG.warn("Received empty response from http call. The endpoint posted to was " + endpointUrl + "\".");
            return emptyResult;
        }

        return extractResultFromResponse(responseBody, resultClass);
    }

    /**
     * Sends the request to the server and returns the streames results.
     * <p/>
     * If the response body is empty, and the status code is successful, the client returns an empty (but valid) result.
     *
     * @param endpointUrl path to the REST service
     * @return the result
     * @throws RuntimeException on marshalling, request, or deserialization failure
     */
    public HttpStreamResponse executeDeleteStream(String endpointUrl) {

        HttpStreamResponse ret = executeDeleteStream(httpClient, endpointUrl);

        if (ret == null) {
            LOG.warn("Received empty response from http call. The endpoint posted to was " + endpointUrl + "\".");
            return null;
        }

        return ret;
    }

    /**
     * Sends the request to the server and returns the deserialized results.
     * <p/>
     * If the response body is empty, and the status code is successful, the client returns an empty (but valid) result.
     *
     * @param requestParamsObject request object
     * @param endpointUrl path to the REST service
     * @param resultClass type of the result class
     * @param emptyResult an instance an empty result.
     * @return the result
     * @throws RuntimeException on marshalling, request, or deserialization failure
     */
    public <T> T executeRequest(Object requestParamsObject, String endpointUrl, Class<T> resultClass, T emptyResult) {

        String postEntity;
        try {
            postEntity = mapper.writeValueAsString(requestParamsObject);
        } catch (IOException e) {
            throw new RuntimeException("Error serializing request parameters object to a string.  Object "
                + "was " + requestParamsObject, e);
        }

        byte[] responseBody = executePostJson(httpClient, endpointUrl, postEntity);

        if (responseBody.length == 0) {
            LOG.warn("Received empty response from http call.  Posted request body was: " + postEntity);
            return emptyResult;
        }

        return extractResultFromResponse(responseBody, resultClass);
    }

    /**
     * Sends the request to the server and returns the deserialized results.
     * <p/>
     * If the response body is empty, and the status code is successful, the client returns an empty (but valid) result.
     *
     * @param requestParamsObject request object
     * @param endpointUrl path to the REST service
     * @param parametrized type of the result class
     * @param parameterClasses parameters of the result class
     * @param emptyResult an instance an empty result.
     * @return the result
     * @throws RuntimeException on marshalling, request, or deserialization failure
     */
    public <T> T executeRequest(Object requestParamsObject, String endpointUrl, Class<T> parametrized, Class<?>[] parameterClasses, T emptyResult) {

        String postEntity;
        try {
            postEntity = mapper.writeValueAsString(requestParamsObject);
        } catch (IOException e) {
            throw new RuntimeException("Error serializing request parameters object to a string.  Object "
                + "was " + requestParamsObject, e);
        }

        byte[] responseBody = executePostJson(httpClient, endpointUrl, postEntity);

        if (responseBody.length == 0) {
            LOG.warn("Received empty response from http call.  Posted request body was: " + postEntity);
            return emptyResult;
        }
        JavaType resultType = mapper.getTypeFactory().constructParametricType(parametrized, parameterClasses);
        return extractResultFromResponse(responseBody, resultType);
    }

    private byte[] executeGetJson(HttpClient httpClient, String endpointUrl) {
        HttpResponse response;
        try {
            response = httpClient.get(endpointUrl);
        } catch (HttpClientException e) {
            throw new RuntimeException("Error posting query request to server.  The endpoint posted to was \"" + endpointUrl + "\".", e);
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

    private byte[] executeDeleteJson(HttpClient httpClient, String endpointUrl) {
        HttpResponse response;
        try {
            response = httpClient.delete(endpointUrl);
        } catch (HttpClientException e) {
            throw new RuntimeException("Error posting query request to server.  The endpoint posted to was \"" + endpointUrl + "\".", e);
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

    private HttpStreamResponse executeGetStream(HttpClient httpClient, String endpointUrl) {
        HttpStreamResponse responseStream = null;
        try {
            responseStream = httpClient.getStream(endpointUrl, DEFAULT_STREAM_TIMEOUT_MILLIS);
        } catch (HttpClientException e) {
            throw new RuntimeException("Error getting query request to server.  The endpoint posted to was \"" + endpointUrl + "\".", e);
        }

        return responseStream;
    }

    private HttpStreamResponse executeDeleteStream(HttpClient httpClient, String endpointUrl) {
        HttpStreamResponse responseStream = null;
        try {
            responseStream = httpClient.deleteStream(endpointUrl, DEFAULT_STREAM_TIMEOUT_MILLIS);
        } catch (HttpClientException e) {
            throw new RuntimeException("Error deleting query request to server.  The endpoint posted to was \"" + endpointUrl + "\".", e);
        }

        return responseStream;
    }

    private byte[] executePostJson(HttpClient httpClient, String endpointUrl, String postEntity) {
        HttpResponse response;
        try {
            response = httpClient.postJson(endpointUrl, postEntity);
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

    private <T> T extractResultFromResponse(byte[] responseBody, JavaType type) {
        T result;
        try {
            result = mapper.readValue(responseBody, 0, responseBody.length, type);
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing response body into result "
                + "object.  Response body was \"" + (responseBody != null ? new String(responseBody, UTF_8) : "null")
                + "\".", e);
        }

        return result;
    }

    private boolean isSuccessStatusCode(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    @Override
    public String toString() {
        return "RequestHelper{" + "httpClient=" + httpClient + '}';
    }
}
