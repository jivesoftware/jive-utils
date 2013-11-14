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
