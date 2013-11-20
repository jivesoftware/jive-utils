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

import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

class ApacheHttpClient31BackedHttpClient implements HttpClient {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger(true);
    private static final String TIMER_NAME = "OutboundHttpRequest";
    private final org.apache.commons.httpclient.HttpClient client;
    private final Map<String, String> headersForEveryRequest;
    private String consumerKey;
    private String consumerSecret;
    private boolean isOauthEnabled = false;
    private boolean isSSLEnabled = false;
    private static final int JSON_POST_LOG_LENGTH_LIMIT = 2048;

    public ApacheHttpClient31BackedHttpClient(org.apache.commons.httpclient.HttpClient client,
        Map<String, String> headersForEveryRequest) {
        this.client = client;
        this.headersForEveryRequest = headersForEveryRequest;
    }

    @Override
    public HttpResponse get(String path) throws HttpClientException {
        return get(path, -1);
    }

    @Override
    public HttpResponse get(String path, int socketTimeoutMillis) throws HttpClientException {
        GetMethod get = new GetMethod(path);
        if (socketTimeoutMillis > 0) {
            get.getParams().setSoTimeout(socketTimeoutMillis);
        }
        try {
            return execute(get);
        } catch (Exception e) {
            throw new HttpClientException("Error executing GET request to: " + client.getHostConfiguration().getHostURL()
                + " path: " + path, e);
        }
    }

    @Override
    public HttpResponse postJson(String path, String postJsonBody) throws HttpClientException {
        return postJson(path, postJsonBody, -1);
    }

    @Override
    public HttpResponse postJson(String path, String postJsonBody, int socketTimeoutMillis) throws HttpClientException {
        try {
            PostMethod post = new PostMethod(path);
            post.setRequestEntity(new StringRequestEntity(postJsonBody, Constants.APPLICATION_JSON_CONTENT_TYPE, "UTF-8"));
            post.setRequestHeader(Constants.CONTENT_TYPE_HEADER_NAME, Constants.APPLICATION_JSON_CONTENT_TYPE);
            if (socketTimeoutMillis > 0) {
                post.getParams().setSoTimeout(socketTimeoutMillis);
            }
            return execute(post);
        } catch (Exception e) {
            String trimmedPostBody =
                (postJsonBody.length() > JSON_POST_LOG_LENGTH_LIMIT) ? postJsonBody.substring(0, JSON_POST_LOG_LENGTH_LIMIT) : postJsonBody;
            throw new HttpClientException("Error executing POST request to: "
                + client.getHostConfiguration().getHostURL() + " path: " + path + " JSON body: " + trimmedPostBody, e);
        }
    }

    @Override
    public HttpResponse postBytes(String path, byte[] postBytes) throws HttpClientException {
        return postBytes(path, postBytes, -1);
    }

    @Override
    public HttpResponse postBytes(String path, byte[] postBytes, int socketTimeoutMillis) throws HttpClientException {
        try {
            PostMethod post = new PostMethod(path);
            post.setRequestEntity(new ByteArrayRequestEntity(postBytes, Constants.APPLICATION_JSON_CONTENT_TYPE));
            post.setRequestHeader(Constants.CONTENT_TYPE_HEADER_NAME, Constants.APPLICATION_OCTET_STREAM_TYPE);
            if (socketTimeoutMillis > 0) {
                post.getParams().setSoTimeout(socketTimeoutMillis);
            }
            return execute(post);
        } catch (Exception e) {
            String trimmedPostBody =
                (postBytes.length > JSON_POST_LOG_LENGTH_LIMIT) ? new String(postBytes, 0, JSON_POST_LOG_LENGTH_LIMIT) : new String(postBytes);
            throw new HttpClientException("Error executing POST request to:"
                + client.getHostConfiguration().getHostURL() + " path: " + path + " byte body: " + trimmedPostBody, e);
        }
    }

    @Override
    public String toString() {
        return "ApacheHttpClient31BackedHttpClient{"
            + "client=" + client
            + ", headersForEveryRequest=" + headersForEveryRequest
            + ", consumerKey=" + consumerKey
            + ", consumerSecret=" + consumerSecret
            + ", isOauthEnabled=" + isOauthEnabled
            + ", isSSLEnabled=" + isSSLEnabled
            + '}';
    }

    private HttpResponse execute(HttpMethod method) throws IOException {
        if (isOauthEnabled) {
            signWithOAuth(method);
        }

        applyHeadersCommonToAllRequests(method);

        byte[] responseBody;
        StatusLine statusLine = null;
        if (LOG.isInfoEnabled()) {
            LOG.startTimer(TIMER_NAME);
        }
        try {
            client.executeMethod(method);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream responseBodyAsStream = method.getResponseBodyAsStream();
            if (responseBodyAsStream != null) {
                IOUtils.copy(responseBodyAsStream, outputStream);
            }

            responseBody = outputStream.toByteArray();
            statusLine = method.getStatusLine();

            return new HttpResponse(statusLine.getStatusCode(), statusLine.getReasonPhrase(), responseBody);

        } finally {
            method.releaseConnection();
            if (LOG.isInfoEnabled()) {
                long elapsedTime = LOG.stopTimer(TIMER_NAME);
                StringBuilder httpInfo = new StringBuilder();
                boolean logInfo = false;
                if (statusLine != null) {
                    logInfo = (statusLine.getStatusCode() < HttpStatus.SC_MULTIPLE_CHOICES);
                    httpInfo.append("Outbound ").append(statusLine.getHttpVersion()).append(" Status ").append(statusLine.getStatusCode());
                } else {
                    httpInfo.append("Exception sending request");
                }
                httpInfo.append(" in ").append(elapsedTime).append(" ms ").append(method.getName()).append(" ")
                    .append(safeHostString(client.getHostConfiguration())).append(method.getURI());
                if (logInfo) {
                    LOG.debug(httpInfo.toString());
                } else {
                    LOG.error(httpInfo.toString());
                }
            }
        }
    }

    private void signWithOAuth(HttpMethod method) throws IOException {
        try {
            HostConfiguration hostConfiguration = client.getHostConfiguration();

            URI uri = method.getURI();
            URI newUri = new URI(
                (isSSLEnabled) ? "https" : "http",
                uri.getUserinfo(),
                hostConfiguration.getHost(),
                hostConfiguration.getPort(),
                uri.getPath(),
                uri.getQuery(),
                uri.getFragment());

            method.setURI(newUri);

            //URI checkUri = method.getURI();
            //String checkUriString = checkUri.toString();

            CommonsHttpOAuthConsumer oAuthConsumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
            oAuthConsumer.setTokenWithSecret(consumerKey, consumerSecret);
            oAuthConsumer.sign(method);
        } catch (Exception e) {
            throw new IOException("Failed to OAuth sign HTTPRequest", e);
        }
    }

    private void applyHeadersCommonToAllRequests(HttpMethod method) {
        for (Map.Entry<String, String> headerEntry : headersForEveryRequest.entrySet()) {
            method.addRequestHeader(headerEntry.getKey(), headerEntry.getValue());
        }
    }

    public void setConsumerTokens(String consumerKey, String consumerSecret) {
        if (StringUtils.isEmpty(consumerKey)) {
            throw new IllegalArgumentException("consumerKey cannot be empty or null.");
        }
        if (StringUtils.isEmpty(consumerSecret)) {
            throw new IllegalArgumentException("consumerSecret cannot be empty or null.");
        }
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        isOauthEnabled = true;

    }

    public boolean isOAuthEnabled() {
        return isOauthEnabled;
    }

    // I hate this! This is a hack to get it working in time!
    public void setUsingSSL() {
        isSSLEnabled = true;
    }

    // package scope for testing ...
    OAuthConsumer getConsumer() {
        if (isOauthEnabled) {
            return new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
        }
        return null;
    }

    void setState(HttpState state) {
        client.setState(state);
    }

    void setHostConfiguration(HostConfiguration hostConfiguration) {
        client.setHostConfiguration(hostConfiguration);
    }

    private String safeHostString(HostConfiguration hostConfiguration) {
        if (hostConfiguration.getHost() != null) {
            return hostConfiguration.getHostURL();
        }
        return "";
    }
}