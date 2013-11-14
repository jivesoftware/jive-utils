/*
 * $Revision: 109733 $
 * $Date: 2010-05-05 10:34:28 -0700 (Wed, 05 May 2010) $
 *
 * Copyright (C) 1999-2011 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.http.client;

import javax.net.ssl.SSLSocketFactory;

public class HttpClientSSLConfig implements HttpClientConfiguration {

    private final boolean useSSL;
    private final SSLSocketFactory customSSLSocketFactory;

    private HttpClientSSLConfig(boolean useSSL, SSLSocketFactory customSSLSocketFactory) {
        this.useSSL = useSSL;
        this.customSSLSocketFactory = customSSLSocketFactory;
    }

    public boolean isUseSsl() {
        return useSSL;
    }

    public SSLSocketFactory getCustomSSLSocketFactory() {
        return customSSLSocketFactory;
    }

    @Override
    public String toString() {
        return "HttpClientConfig{"
            + ", customSSLSocketFactory=" + customSSLSocketFactory
            + '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    final public static class Builder {

        private boolean useSSL = false;
        private SSLSocketFactory customSSLSocketFactory = null;

        private Builder() {
        }

        public Builder setUseSSL(boolean useSSL) {
            this.useSSL = useSSL;
            return this;
        }

        public Builder setUseSslWithCustomSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
            if (sslSocketFactory == null) {
                throw new IllegalArgumentException("sslSocketFactory cannot be null");
            }
            this.useSSL = true;
            this.customSSLSocketFactory = sslSocketFactory;
            return this;
        }

        public HttpClientSSLConfig build() {
            return new HttpClientSSLConfig(useSSL, customSSLSocketFactory);
        }
    }
}
