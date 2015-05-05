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
package com.jivesoftware.os.jive.utils.http.client;

import java.util.Map;

public class NoClientAvailableHttpClient implements HttpClient {

    @Override
    public HttpResponse get(String path) throws HttpClientException {
        return new HttpResponse(503, "There is no available client to support the get call.", new byte[0]);
    }

    @Override
    public HttpResponse postJson(String path, String postJsonBody) throws HttpClientException {
        return new HttpResponse(503, "There is no available client to support the postJson call.", new byte[0]);
    }

    @Override
    public HttpResponse postBytes(String path, byte[] postBytes) throws HttpClientException {
        return new HttpResponse(503, "There is no available client to support the postBytes call.", new byte[0]);
    }

    @Override
    public HttpResponse get(String path, int socketTimeoutMillis) throws HttpClientException {
        return new HttpResponse(503, "There is no available client to support the get call.", new byte[0]);
    }

    public HttpResponse get(HttpRequestParams requestParams) throws HttpClientException {
        return new HttpResponse(503, "There is no available client to support the get call.", new byte[0]);
    }

    @Override
    public HttpResponse postJson(String path, String postJsonBody, int socketTimeoutMillis) throws HttpClientException {
        return new HttpResponse(503, "There is no available client to support the postJson call.", new byte[0]);
    }

    public HttpResponse postJson(HttpRequestParams requestParams, String postJsonBody) throws HttpClientException {
        return new HttpResponse(503, "There is no available client to support the postJson call.", new byte[0]);
    }

    @Override
    public HttpResponse postBytes(String path, byte[] postBytes, int socketTimeoutMillis) throws HttpClientException {
        return new HttpResponse(503, "There is no available client to support the postBytes call.", new byte[0]);
    }

    public HttpResponse postBytes(HttpRequestParams requestParams, byte[] postBytes) throws HttpClientException {
        return new HttpResponse(503, "There is no available client to support the postBytes call.", new byte[0]);
    }

    @Override
    public HttpResponse get(String path, Map<String, String> headers) throws HttpClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpStreamResponse getStream(String path) throws HttpClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpResponse get(String path, Map<String, String> headers, int timeoutMillis) throws HttpClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpStreamResponse getStream(String path, int timeoutMillis) throws HttpClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpResponse postJson(String path, String postJsonBody, Map<String, String> headers) throws HttpClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpResponse postJson(String path, String postJsonBody, Map<String, String> headers, int timeoutMillis) throws HttpClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpResponse delete(String path) throws HttpClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpResponse delete(String path, Map<String, String> headers) throws HttpClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpStreamResponse deleteStream(String path) throws HttpClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpResponse delete(String path, int timeoutMillis) throws HttpClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpResponse delete(String path, Map<String, String> headers, int timeoutMillis) throws HttpClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpStreamResponse deleteStream(String path, int timeoutMillis) throws HttpClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpResponse putJson(String path, String putJsonBody) throws HttpClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpResponse putJson(String path, String putJsonBody, Map<String, String> headers) throws HttpClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpResponse putJson(String path, String putJsonBody, int timeoutMillis) throws HttpClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpResponse putJson(String path, String putJsonBody, Map<String, String> headers, int timeoutMillis) throws HttpClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpResponse putBytes(String path, byte[] putBytes) throws HttpClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HttpResponse putBytes(String path, byte[] putBytes, int timeoutMillis) throws HttpClientException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
