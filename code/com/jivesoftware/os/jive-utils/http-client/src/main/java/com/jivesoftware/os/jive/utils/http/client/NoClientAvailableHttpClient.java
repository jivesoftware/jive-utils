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
        return new HttpResponse(503, "There is no available client to support the postBytes call.", new byte[0]);
    }

    @Override
    public HttpResponse postJson(String path, String postJsonBody, int socketTimeoutMillis) throws HttpClientException {
        return new HttpResponse(503, "There is no available client to support the postBytes call.", new byte[0]);
    }

    @Override
    public HttpResponse postBytes(String path, byte[] postBytes, int socketTimeoutMillis) throws HttpClientException {
        return new HttpResponse(503, "There is no available client to support the postBytes call.", new byte[0]);
    }
}
