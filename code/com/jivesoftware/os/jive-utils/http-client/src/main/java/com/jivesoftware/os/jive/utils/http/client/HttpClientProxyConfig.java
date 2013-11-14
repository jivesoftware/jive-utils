/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */

package com.jivesoftware.os.jive.utils.http.client;

/**
 *
 */
public class HttpClientProxyConfig implements HttpClientConfiguration {
    private final String proxyHost;
    private final int proxyPort;
    private final String proxyUsername;
    private final String proxyPassword;

    private HttpClientProxyConfig(
        String proxyHost, int proxyPort, String proxyUsername, String proxyPassword) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    @Override
    public String toString() {
        return "HttpClientConfig{"
            + ", proxyHost=" + proxyHost
            + ", proxyPort=" + proxyPort
            + ", proxyUsername=" + proxyUsername
            + ", proxyPassword=" + "*******" // dont expose password in logs
            + '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    final public static class Builder {

        private String proxyHost = "";
        private int proxyPort = -1;
        private String proxyUsername = "";
        private String proxyPassword = "";

        private Builder() {
        }

        public Builder setProxyConfiguration(String proxyHost, int proxyPort, String proxyUsername, String proxyPassword) {
            this.proxyHost = proxyHost;
            this.proxyPort = proxyPort;
            this.proxyUsername = proxyUsername;
            this.proxyPassword = proxyPassword;
            return this;
        }

        public HttpClientProxyConfig build() {
            return new HttpClientProxyConfig(proxyHost, proxyPort, proxyUsername, proxyPassword);
        }
    }
}
