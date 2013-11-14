/*
 * $Revision: 109733 $
 * $Date: 2010-05-05 10:34:28 -0700 (Wed, 05 May 2010) $
 *
 * Copyright (C) 1999-2011 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.http.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final public class HttpClientConfig implements HttpClientConfiguration {

    private final int socketTimeoutInMillis;
    private final int maxConnections;
    private final int maxConnectionsPerHost;
    private final Map<String, String> headersForEveryRequest;

    private HttpClientConfig(int socketTimeoutInMillis, int maxConnections, int maxConnectionsPerHost,
        Map<String, String> headersForEveryRequest) {
        this.socketTimeoutInMillis = socketTimeoutInMillis;
        this.maxConnections = maxConnections;
        this.maxConnectionsPerHost = maxConnectionsPerHost;
        this.headersForEveryRequest = new HashMap<>(headersForEveryRequest);
    }

    public int getSocketTimeoutInMillis() {
        return socketTimeoutInMillis;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public int getMaxConnectionsPerHost() {
        return maxConnectionsPerHost;
    }

    public Map<String, String> getCopyOfHeadersForEveryRequest() {
        return new HashMap<>(headersForEveryRequest);
    }

    @Override
    public String toString() {
        return "HttpClientConfig{" + "socketTimeoutInMillis=" + socketTimeoutInMillis + ", maxConnections=" +
            maxConnections + ", maxConnectionsPerHost=" + maxConnectionsPerHost + ", headersForEveryRequest=" +
            headersForEveryRequest + '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    final public static class Builder {

        private int socketTimeoutInMillis = -1;
        private int maxConnections = -1;
        private int maxConnectionsPerHost = -1;
        private Map<String, String> headersForEveryRequest = Collections.emptyMap();

        private Builder() {
        }

        public Builder setSocketTimeoutInMillis(int socketTimeoutInMillis) {
            this.socketTimeoutInMillis = socketTimeoutInMillis;
            return this;
        }

        public Builder setMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public Builder setMaxConnectionsPerHost(int maxConnectionsPerHost) {
            this.maxConnectionsPerHost = maxConnectionsPerHost;
            return this;
        }

        public Builder setHeadersForEveryRequest(Map<String, String> headersForEveryRequest) {
            this.headersForEveryRequest = headersForEveryRequest;
            return this;
        }

        public HttpClientConfig build() {
            return new HttpClientConfig(
                socketTimeoutInMillis, maxConnections, maxConnectionsPerHost,
                headersForEveryRequest);
        }
    }
}
