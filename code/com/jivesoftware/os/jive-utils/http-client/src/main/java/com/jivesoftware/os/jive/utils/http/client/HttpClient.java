/*
 * $Revision: 109733 $
 * $Date: 2010-05-05 10:34:28 -0700 (Wed, 05 May 2010) $
 *
 * Copyright (C) 1999-2011 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
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
