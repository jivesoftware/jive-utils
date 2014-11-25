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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp3.CommonsHttp3OAuthConsumer;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.ContentDisposition;

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
        return get(path, null, -1);
    }

    @Override
    public HttpResponse get(String path, Map<String, String> headers) throws HttpClientException {
        return get(path, headers, -1);
    }

    @Override
    public HttpResponse get(String path, int timeoutMillis) throws HttpClientException {
        return get(path, null, timeoutMillis);
    }

    @Override
    public HttpResponse get(String path, Map<String, String> headers, int timeoutMillis) throws HttpClientException {
        return executeMethod(new GetMethod(path), headers, timeoutMillis);
    }

    @Override
    public HttpStreamResponse getStream(String path) throws HttpClientException {
        return getStream(path, -1);
    }

    @Override
    public HttpStreamResponse getStream(String path, int timeoutMillis) throws HttpClientException {
        return executeMethodStream(new DeleteMethod(path), timeoutMillis);
    }

    @Override
    public HttpResponse delete(String path) throws HttpClientException {
        return delete(path, null, -1);
    }

    @Override
    public HttpResponse delete(String path, Map<String, String> headers) throws HttpClientException {
        return delete(path, headers, -1);
    }

    @Override
    public HttpResponse delete(String path, int timeoutMillis) throws HttpClientException {
        return delete(path, null, timeoutMillis);
    }

    @Override
    public HttpResponse delete(String path, Map<String, String> headers, int timeoutMillis) throws HttpClientException {
        return executeMethod(new DeleteMethod(path), headers, timeoutMillis);
    }

    @Override
    public HttpStreamResponse deleteStream(String path) throws HttpClientException {
        return deleteStream(path, -1);
    }

    @Override
    public HttpStreamResponse deleteStream(String path, int timeoutMillis) throws HttpClientException {
        return executeMethodStream(new DeleteMethod(path), timeoutMillis);
    }

    @Override
    public HttpResponse postJson(String path, String postJsonBody) throws HttpClientException {
        return postJson(path, postJsonBody, null, -1);
    }

    @Override
    public HttpResponse postJson(String path, String postJsonBody, Map<String, String> headers) throws HttpClientException {
        return postJson(path, postJsonBody, headers, -1);
    }

    @Override
    public HttpResponse postJson(String path, String postJsonBody, int timeoutMillis) throws HttpClientException {
        return postJson(path, postJsonBody, null, timeoutMillis);
    }

    @Override
    public HttpResponse postJson(String path, String postJsonBody, Map<String, String> headers, int timeoutMillis) throws HttpClientException {
        return executeMethodJson(new PostMethod(path), postJsonBody, headers, timeoutMillis);
    }

    @Override
    public HttpResponse postBytes(String path, byte[] postBytes) throws HttpClientException {
        return postBytes(path, postBytes, -1);
    }

    @Override
    public HttpResponse postBytes(String path, byte[] postBytes, int timeoutMillis) throws HttpClientException {
        return executeMethodBytes(new PostMethod(path), postBytes, timeoutMillis);
    }

    @Override
    public HttpResponse putJson(String path, String putJsonBody) throws HttpClientException {
        return putJson(path, putJsonBody, null, -1);
    }

    @Override
    public HttpResponse putJson(String path, String putJsonBody, Map<String, String> headers) throws HttpClientException {
        return putJson(path, putJsonBody, headers, -1);
    }

    @Override
    public HttpResponse putJson(String path, String putJsonBody, int timeoutMillis) throws HttpClientException {
        return putJson(path, putJsonBody, null, timeoutMillis);
    }

    @Override
    public HttpResponse putJson(String path, String putJsonBody, Map<String, String> headers, int timeoutMillis) throws HttpClientException {
        return executeMethodJson(new PutMethod(path), putJsonBody, headers, timeoutMillis);
    }

    @Override
    public HttpResponse putBytes(String path, byte[] putBytes) throws HttpClientException {
        return putBytes(path, putBytes, -1);
    }

    @Override
    public HttpResponse putBytes(String path, byte[] putBytes, int timeoutMillis) throws HttpClientException {
        return executeMethodBytes(new PutMethod(path), putBytes, timeoutMillis);
    }

    private HttpResponse executeMethodJson(EntityEnclosingMethod method, String jsonBody, Map<String, String> headers, int timeoutMillis)
        throws HttpClientException {
        try {
            setRequestHeaders(headers, method);

            method.setRequestEntity(new StringRequestEntity(jsonBody, Constants.APPLICATION_JSON_CONTENT_TYPE, "UTF-8"));
            method.setRequestHeader(Constants.CONTENT_TYPE_HEADER_NAME, Constants.APPLICATION_JSON_CONTENT_TYPE);
            if (timeoutMillis > 0) {
                return executeWithTimeout(method, timeoutMillis);
            } else {
                return execute(method);
            }
        } catch (Exception e) {
            String trimmedMethodBody = (jsonBody.length() > JSON_POST_LOG_LENGTH_LIMIT) ?
                jsonBody.substring(0, JSON_POST_LOG_LENGTH_LIMIT) : jsonBody;
            throw new HttpClientException("Error executing " + method.getName() + " request to: "
                + client.getHostConfiguration().getHostURL() + " path: " + method.getPath() + " JSON body: " + trimmedMethodBody, e);
        }
    }

    private HttpResponse executeMethodBytes(EntityEnclosingMethod method, byte[] putBytes, int timeoutMillis) throws HttpClientException {
        try {
            method.setRequestEntity(new ByteArrayRequestEntity(putBytes, Constants.APPLICATION_JSON_CONTENT_TYPE));
            method.setRequestHeader(Constants.CONTENT_TYPE_HEADER_NAME, Constants.APPLICATION_OCTET_STREAM_TYPE);
            if (timeoutMillis > 0) {
                return executeWithTimeout(method, timeoutMillis);
            } else {
                return execute(method);
            }
        } catch (Exception e) {
            String trimmedMethodBody = (putBytes.length > JSON_POST_LOG_LENGTH_LIMIT) ?
                    new String(putBytes, 0, JSON_POST_LOG_LENGTH_LIMIT) : new String(putBytes);
            throw new HttpClientException("Error executing " + method.getName() + " request to:"
                    + client.getHostConfiguration().getHostURL() + " path: " + method.getPath() + " byte body: " + trimmedMethodBody, e);
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

    private HttpResponse executeMethod(HttpMethodBase method, Map<String, String> headers, int timeoutMillis) throws HttpClientException {
        setRequestHeaders(headers, method);

        if (timeoutMillis > 0) {
            return executeWithTimeout(method, timeoutMillis);
        }
        try {
            return execute(method);
        } catch (Exception e) {
            throw new HttpClientException("Error executing " + method.getName() + " request to: " + client.getHostConfiguration().getHostURL()
                + " path: " + method.getPath(), e);
        }
    }

    private HttpStreamResponse executeMethodStream(HttpMethodBase method, int timeoutMillis) throws HttpClientException {
        try {
            return executeStreamWithTimeout(method, timeoutMillis);
        } catch (Exception e) {
            throw new HttpClientException("Error executing " + method.getName() + " request to: " + client.getHostConfiguration().getHostURL()
                + " path: " + method.getPath(), e);
        }
    }

    private HttpResponse executeWithTimeout(final HttpMethodBase httpMethod, int timeoutMillis) {
        client.getParams().setParameter("http.method.retry-handler", new DefaultHttpMethodRetryHandler(0, false));
        ExecutorService service = Executors.newSingleThreadExecutor();

        Future<HttpResponse> future = service.submit(new Callable<HttpResponse>() {
            @Override
            public HttpResponse call() throws IOException {
                return execute(httpMethod);
            }
        });

        try {
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            String uriInfo = "";
            try {
                uriInfo = " for " + httpMethod.getURI();
            } catch (Exception ie) {
            }
            LOG.warn("Http connection thread was interrupted or has timed out" + uriInfo, e);
            return new HttpResponse(HttpStatus.SC_REQUEST_TIMEOUT, "Request Timeout", null);
        } finally {
            service.shutdownNow();
        }
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
                    LOG.info(httpInfo.toString());
                } else {
                    LOG.error(httpInfo.toString());
                }
            }
        }
    }

    private HttpStreamResponse executeStreamWithTimeout(final HttpMethodBase HttpMethod, int timeoutMillis) {
        client.getParams().setParameter("http.method.retry-handler", new DefaultHttpMethodRetryHandler(0, false));
        ExecutorService service = Executors.newSingleThreadExecutor();

        Future<HttpStreamResponse> future = service.submit(new Callable<HttpStreamResponse>() {
            @Override
            public HttpStreamResponse call() throws IOException {
                return executeStream(HttpMethod);
            }
        });

        try {
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            String uriInfo = "";
            try {
                uriInfo = " for " + HttpMethod.getURI();
            } catch (Exception ie) {
            }
            LOG.warn("Http connection thread was interrupted or has timed out" + uriInfo, e);
            return null;
        } finally {
            service.shutdownNow();
        }
    }

    private HttpStreamResponse executeStream(HttpMethod method) throws IOException {
        if (isOauthEnabled) {
            signWithOAuth(method);
        }

        applyHeadersCommonToAllRequests(method);

        StatusLine statusLine = null;
        if (LOG.isInfoEnabled()) {
            LOG.startTimer(TIMER_NAME);
        }
        try {
            int status = client.executeMethod(method);

            checkStreamStatus(status, method);

            return createStreamResponse(method);
        } catch (Exception e) {
            throw new IOException("Failed to get stream", e);
        } finally {

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
                    LOG.info(httpInfo.toString());
                } else {
                    LOG.error(httpInfo.toString());
                }
            }
        }
    }

    private void checkStreamStatus(int status, HttpMethod httpMethod) throws HttpClientException {
        LOG.debug(String.format("Got status: %s %s", status, httpMethod.getStatusText()));
        if (status != 200 && status != 201) {
            try {
                String responseBodyAsString = httpMethod.getResponseBodyAsString();
                if (!StringUtils.isEmpty(responseBodyAsString)) {
                    responseBodyAsString = new String(responseBodyAsString.getBytes(), "UTF-8");
                    throw new HttpClientException(
                            "Bad status : " + httpMethod.getStatusText() + ":\n" + responseBodyAsString);
                } else {
                    throw new HttpClientException("Bad status : " + httpMethod.getStatusLine());
                }
            } catch (Exception e) {
                throw new HttpClientException("Bad status : " + status + ". Could not read response body.");
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
            CommonsHttp3OAuthConsumer oAuthConsumer = new CommonsHttp3OAuthConsumer(consumerKey, consumerSecret);
            oAuthConsumer.setTokenWithSecret(consumerKey, consumerSecret);
            oAuthConsumer.sign(method);
        } catch (Exception e) {
            throw new IOException("Failed to OAuth sign HTTPRequest", e);
        }
    }

    private void setRequestHeaders(Map<String, String> headers, HttpMethodBase method) {
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                method.setRequestHeader(header.getKey(), header.getValue());
            }
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
            return new CommonsHttp3OAuthConsumer(consumerKey, consumerSecret);
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

    private HttpStreamResponse createStreamResponse(HttpMethod method) throws IOException {

        StatusLine statusLine = method.getStatusLine();
        String filename = getFileName(method);
        long length = getContentLength(method);
        String contentType = getContentType(method);

        HttpStreamResponse streamResponse = new HttpStreamResponse(statusLine.getStatusCode(), statusLine.getReasonPhrase(), method.getResponseBodyAsStream(),
                filename, contentType, length);
        return streamResponse;
    }


    /*
     helper methods to get data from headers
     */
    private String getFileName(HttpMethod method) {
        String filename;
        filename = GetFileNameFromContentDisposition(method);
        if (StringUtils.isNotEmpty(filename)) {
            return filename;
        } else {
            filename = getFileNameFromURL(method);
        }
        return filename;

    }

    private String GetFileNameFromContentDisposition(HttpMethod method) {
        String filename = "";
        ContentDisposition contentDisposition;
        Header[] contentDispositionHeader = method.getResponseHeaders("Content-Disposition");
        if (contentDispositionHeader != null && contentDispositionHeader.length == 1 && contentDispositionHeader[0] != null
                && contentDispositionHeader[0].getValue() != null) {
            try {
                contentDisposition = new ContentDisposition(contentDispositionHeader[0].getValue());
                filename = contentDisposition.getFileName();
            } catch (java.text.ParseException e) {
                LOG.info("cant parse content disposition", e);
            }
        }
        return filename;
    }

    private String getFileNameFromURL(HttpMethod method) {
        String filename = "";
        try {
            String baseName = FilenameUtils.getBaseName(method.getURI().getURI());
            String extension = FilenameUtils.getExtension(method.getURI().getURI());
            extension = extension.substring(0, extension.indexOf("&") < 0 ? extension.length() : extension.indexOf("&"));
            extension = extension.substring(0, extension.indexOf("?") < 0 ? extension.length() : extension.indexOf("?"));
            if (StringUtils.isNotEmpty(baseName) && StringUtils.isNotEmpty(extension)) {
                filename = baseName + "." + extension;
            }
        } catch (URIException e) {
            LOG.info("cant parse url for getting filename", e);
        }
        return filename;
    }

    private long getContentLength(HttpMethod method) {
        long size = -1;
        Header[] contentLengthHeader = method.getResponseHeaders("Content-Length");
        if (contentLengthHeader != null && contentLengthHeader.length == 1 && contentLengthHeader[0] != null
                && contentLengthHeader[0].getValue() != null) {
            try {
                String contentLength = contentLengthHeader[0].getValue();
                size = Long.parseLong(contentLength);
            } catch (NumberFormatException e) {
                LOG.info("cant parse content Content-Length", e);
            }
        }
        return size;
    }

    private String getContentType(HttpMethod method) {
        String contentType = "application/octet-stream";
        Header[] contentTypeHeaders = method.getResponseHeaders("Content-Type");
        if (contentTypeHeaders != null && contentTypeHeaders.length == 1 && contentTypeHeaders[0] != null && contentTypeHeaders[0].getValue() != null) {
            contentType = contentTypeHeaders[0].getValue();
        }
        return contentType;
    }
}
