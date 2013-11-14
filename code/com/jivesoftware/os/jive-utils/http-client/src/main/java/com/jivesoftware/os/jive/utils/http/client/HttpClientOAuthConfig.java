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
public class HttpClientOAuthConfig implements HttpClientConfiguration {

    private final String serviceName;
    private final HttpClientConsumerKeyAndSecretProvider consumerKeyAndSecretProvider;

    private HttpClientOAuthConfig(String serviceName, HttpClientConsumerKeyAndSecretProvider consumerKeyAndSecretProvider) {
        this.serviceName = serviceName;
        this.consumerKeyAndSecretProvider = consumerKeyAndSecretProvider;
    }

    public String getServiceName() {
        return serviceName;
    }

    public HttpClientConsumerKeyAndSecretProvider getConsumerKeyAndSecretProvider() {
        return consumerKeyAndSecretProvider;
    }

    @Override
    public String toString() {
        return "HttpClientOAuthConfig{" + "serviceName=" + serviceName + ", consumerKeyAndSecretProvider=" + consumerKeyAndSecretProvider + '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    final public static class Builder {

        private String serviceName = null;
        private HttpClientConsumerKeyAndSecretProvider consumerKeyAndSecretProvider;

        private Builder() {
        }

        public Builder setOAuthInfo(String serviceName, HttpClientConsumerKeyAndSecretProvider consumerKeyAndSecretProvider) {
            if (serviceName == null) {
                throw new IllegalArgumentException("serviceName cannot be null");
            }
            if (consumerKeyAndSecretProvider == null) {
                throw new IllegalArgumentException("consumerKeyAndSecretProvider cannot be null");
            }

            this.serviceName = serviceName;
            this.consumerKeyAndSecretProvider = consumerKeyAndSecretProvider;
            return this;
        }

        public HttpClientOAuthConfig build() {
            return new HttpClientOAuthConfig(serviceName, consumerKeyAndSecretProvider);
        }
    }
}
