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

public interface HttpClient {
    //TODO need to create request object to pass as param to these methods.

    /**
     *
     * @param path everything but the leading "http/s://host:port"
     * @return
     * @throws HttpClientException
     */
    HttpResponse get(String path) throws HttpClientException;

    /**
     *
     * @param path everything but the leading "http/s://host:port"
     * @param socketTimeoutMillis
     * @return
     * @throws HttpClientException
     */
    HttpResponse get(String path, int socketTimeoutMillis) throws HttpClientException;

    /**
     *
     * @param path everything but the leading "http/s://host:port"
     * @param postJsonBody
     * @return
     * @throws HttpClientException
     */
    HttpResponse postJson(String path, String postJsonBody) throws HttpClientException;

    /**
     *
     * @param path everything but the leading "http/s://host:port"
     * @param postJsonBody
     * @param socketTimeoutMillis
     * @return
     * @throws HttpClientException
     */
    HttpResponse postJson(String path, String postJsonBody, int socketTimeoutMillis) throws HttpClientException;

    /**
     *
     * @param path everything but the leading "http/s://host:port"
     * @param postBytes
     * @return
     * @throws HttpClientException
     */
    HttpResponse postBytes(String path, byte[] postBytes) throws HttpClientException;

    /**
     *
     * @param path everything but the leading "http/s://host:port"
     * @param postBytes
     * @param socketTimeoutMillis
     * @return
     * @throws HttpClientException
     */
    HttpResponse postBytes(String path, byte[] postBytes, int socketTimeoutMillis) throws HttpClientException;
}
