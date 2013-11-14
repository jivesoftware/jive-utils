/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.http.client;

import java.util.Collection;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang.StringUtils;

public class HttpClientFactoryProvider {

    private static final String HTTPS_PROTOCOL = "https";
    private static final int SSL_PORT = 443;

    public HttpClientFactory createHttpClientFactory(final Collection<HttpClientConfiguration> configurations) {
        return new HttpClientFactory() {
            @Override
            public HttpClient createClient(String host, int port) {

                ApacheHttpClient31BackedHttpClient httpClient = createApacheClient();

                HostConfiguration hostConfiguration = new HostConfiguration();
                configureSsl(hostConfiguration, host, port, httpClient);
                configureProxy(hostConfiguration, httpClient);

                httpClient.setHostConfiguration(hostConfiguration);
                configureOAuth(httpClient);
                return httpClient;
            }

            private ApacheHttpClient31BackedHttpClient createApacheClient() {
                HttpClientConfig httpClientConfig = locateConfig(HttpClientConfig.class, HttpClientConfig.newBuilder().build());

                HttpConnectionManager connectionManager = createConnectionManager(httpClientConfig);

                org.apache.commons.httpclient.HttpClient client =
                    new org.apache.commons.httpclient.HttpClient(connectionManager);
                client.getParams().setParameter(HttpMethodParams.COOKIE_POLICY, CookiePolicy.RFC_2109);
                client.getParams().setParameter(HttpMethodParams.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
                client.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
                client.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, false);
                client.getParams().setBooleanParameter(HttpConnectionParams.STALE_CONNECTION_CHECK, true);
                client.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT,
                    httpClientConfig.getSocketTimeoutInMillis() > 0 ? httpClientConfig.getSocketTimeoutInMillis() : 0);
                client.getParams().setParameter(HttpConnectionParams.SO_TIMEOUT,
                    httpClientConfig.getSocketTimeoutInMillis() > 0 ? httpClientConfig.getSocketTimeoutInMillis() : 0);

                return new ApacheHttpClient31BackedHttpClient(client, httpClientConfig.getCopyOfHeadersForEveryRequest());

            }

            @SuppressWarnings("unchecked")
            private <T> T locateConfig(Class<? extends T> _class, T defaultConfiguration) {
                for (HttpClientConfiguration configuration : configurations) {
                    if (_class.isInstance(configuration)) {
                        return (T) configuration;
                    }
                }
                return defaultConfiguration;
            }

            private boolean hasValidProxyUsernameAndPasswordSettings(HttpClientProxyConfig httpClientProxyConfig) {
                return StringUtils.isNotBlank(httpClientProxyConfig.getProxyUsername()) && StringUtils.isNotBlank(httpClientProxyConfig.getProxyPassword());
            }

            private HttpConnectionManager createConnectionManager(HttpClientConfig config) {
                MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
                if (config.getMaxConnectionsPerHost() > 0) {
                    connectionManager.getParams().setDefaultMaxConnectionsPerHost(config.getMaxConnectionsPerHost());
                } else {
                    connectionManager.getParams().setDefaultMaxConnectionsPerHost(Integer.MAX_VALUE);
                }
                if (config.getMaxConnections() > 0) {
                    connectionManager.getParams().setMaxTotalConnections(config.getMaxConnections());
                }
                return connectionManager;
            }

            private void configureOAuth(ApacheHttpClient31BackedHttpClient httpClient) {
                HttpClientOAuthConfig httpClientOAuthConfig = locateConfig(HttpClientOAuthConfig.class, null);
                if (httpClientOAuthConfig != null) {
                    String serviceName = httpClientOAuthConfig.getServiceName();
                    HttpClientConsumerKeyAndSecretProvider consumerKeyAndSecretProvider = httpClientOAuthConfig.getConsumerKeyAndSecretProvider();
                    String consumerKey = consumerKeyAndSecretProvider.getConsumerKey(serviceName);
                    if (StringUtils.isEmpty(consumerKey)) {
                        throw new RuntimeException("could create oauth client because consumerKey is null or empty for service:" + serviceName);
                    }
                    String consumerSecret = consumerKeyAndSecretProvider.getConsumerSecret(serviceName);
                    if (StringUtils.isEmpty(consumerSecret)) {
                        throw new RuntimeException("could create oauth client because consumerSecret is null or empty for service:" + serviceName);
                    }

                    httpClient.setConsumerTokens(consumerKey, consumerSecret);
                }
            }

            private void configureProxy(HostConfiguration hostConfiguration, ApacheHttpClient31BackedHttpClient httpClient) {
                HttpClientProxyConfig httpClientProxyConfig = locateConfig(HttpClientProxyConfig.class, null);
                if (httpClientProxyConfig != null) {
                    hostConfiguration.setProxy(httpClientProxyConfig.getProxyHost(), httpClientProxyConfig.getProxyPort());
                    if (hasValidProxyUsernameAndPasswordSettings(httpClientProxyConfig)) {
                        HttpState state = new HttpState();
                        state.setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(httpClientProxyConfig.getProxyUsername(),
                            httpClientProxyConfig.getProxyPassword()));
                        httpClient.setState(state);
                    }
                }
            }

            private void configureSsl(HostConfiguration hostConfiguration, String host, int port, ApacheHttpClient31BackedHttpClient httpClient)
                throws IllegalStateException {
                HttpClientSSLConfig httpClientSSLConfig = locateConfig(HttpClientSSLConfig.class, null);
                if (httpClientSSLConfig != null) {
                    Protocol sslProtocol;
                    if (httpClientSSLConfig.getCustomSSLSocketFactory() != null) {
                        sslProtocol = new Protocol(HTTPS_PROTOCOL,
                            new CustomSecureProtocolSocketFactory(httpClientSSLConfig.getCustomSSLSocketFactory()), SSL_PORT);
                    } else {
                        sslProtocol = Protocol.getProtocol(HTTPS_PROTOCOL);
                    }
                    hostConfiguration.setHost(host, port, sslProtocol);
                    httpClient.setUsingSSL();
                } else {
                    hostConfiguration.setHost(host, port);
                }
            }
        };
    }
}
