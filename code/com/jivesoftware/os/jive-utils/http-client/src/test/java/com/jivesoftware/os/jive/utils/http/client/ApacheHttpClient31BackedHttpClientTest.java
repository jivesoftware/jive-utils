package com.jivesoftware.os.jive.utils.http.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApacheHttpClient31BackedHttpClientTest {

    private org.apache.commons.httpclient.HttpClient httpClient;

    @BeforeMethod
    public void setUp() throws Exception {
        httpClient = mock(org.apache.commons.httpclient.HttpClient.class);
        when(httpClient.getHostConfiguration()).thenReturn(new HostConfiguration());
    }

    @Test
    public void testGet() throws Exception {
        ApacheHttpClient31BackedHttpClient client = new ApacheHttpClient31BackedHttpClient(httpClient,
            new HashMap<String, String>());

        final String path = "/path/to/endpoint";
        byte[] responseBytes = "reponse".getBytes("UTF-8");
        int responseStatus = 200;
        String responseLine = "blah";

        mockResponse(responseBytes, responseStatus, responseLine);

        HttpResponse response = client.get(path);

        verify(httpClient).executeMethod(argThat(new ArgumentMatcher<HttpMethod>() {
            @Override
            public boolean matches(Object o) {
                try {
                    return o instanceof GetMethod && pathMatches((GetMethod) o, path);
                } catch (URIException e) {
                    return false;
                }
            }
        }));

        Assert.assertEquals(response.getResponseBody(), responseBytes);
        Assert.assertEquals(response.getStatusCode(), responseStatus);
        Assert.assertEquals(response.getStatusReasonPhrase(), responseLine);
    }

    @Test(expectedExceptions = HttpClientException.class)
    public void testGetThrowsException() throws Exception {
        ApacheHttpClient31BackedHttpClient client = new ApacheHttpClient31BackedHttpClient(httpClient, new HashMap<String, String>());

        final String path = "/path/to/endpoint";

        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setHost("foobar.com", 3456, Protocol.getProtocol("http"));
        when(httpClient.getHostConfiguration()).thenReturn(hostConfiguration);
        when(httpClient.executeMethod(Matchers.<HttpMethod>any())).thenThrow(new IOException());

        client.get(path);
    }

    @Test
    public void testPostJsonString() throws Exception {
        ApacheHttpClient31BackedHttpClient client = new ApacheHttpClient31BackedHttpClient(httpClient,
            new HashMap<String, String>());

        final String path = "/path/to/endpoint";
        final String payload = "payload....";

        byte[] responseBytes = "reponse".getBytes("UTF-8");
        int responseStatus = 200;
        String responseLine = "blah";

        mockResponse(responseBytes, responseStatus, responseLine);

        HttpResponse response = client.postJson(path, payload);

        verify(httpClient).executeMethod(argThat(new PostMethodArgumentMatcher(path,
            payload.getBytes("UTF-8"),
            Constants.APPLICATION_JSON_CONTENT_TYPE,
            Constants.APPLICATION_JSON_CONTENT_TYPE + "; charset=UTF-8")));

        Assert.assertEquals(response.getResponseBody(), responseBytes);
        Assert.assertEquals(response.getStatusCode(), responseStatus);
        Assert.assertEquals(response.getStatusReasonPhrase(), responseLine);
    }

    @Test(expectedExceptions = HttpClientException.class)
    public void testPostJsonStringThrowsException() throws Exception {
        ApacheHttpClient31BackedHttpClient client = new ApacheHttpClient31BackedHttpClient(httpClient, new HashMap<String, String>());

        final String path = "/path/to/endpoint";

        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setHost("foobar.com", 3456, Protocol.getProtocol("https"));
        when(httpClient.getHostConfiguration()).thenReturn(hostConfiguration);
        when(httpClient.executeMethod(Matchers.<HttpMethod>any())).thenThrow(new IOException());

        client.postJson(path, "body");
    }

    @Test
    public void testPostJsonBytes() throws Exception {
        ApacheHttpClient31BackedHttpClient client = new ApacheHttpClient31BackedHttpClient(httpClient,
            new HashMap<String, String>());

        final String path = "/path/to/endpoint";
        final byte[] payload = "payload....".getBytes("UTF-8");

        byte[] responseBytes = "reponse".getBytes("UTF-8");
        int responseStatus = 200;
        String responseLine = "blah";

        mockResponse(responseBytes, responseStatus, responseLine);

        HttpResponse response = client.postBytes(path, payload);

        verify(httpClient).executeMethod(argThat(new PostMethodArgumentMatcher(path,
            payload,
            Constants.APPLICATION_OCTET_STREAM_TYPE,
            Constants.APPLICATION_JSON_CONTENT_TYPE)));

        Assert.assertEquals(response.getResponseBody(), responseBytes);
        Assert.assertEquals(response.getStatusCode(), responseStatus);
        Assert.assertEquals(response.getStatusReasonPhrase(), responseLine);
    }

    @Test(expectedExceptions = HttpClientException.class)
    public void testPostJsonBytesThrowsException() throws Exception {
        ApacheHttpClient31BackedHttpClient client = new ApacheHttpClient31BackedHttpClient(httpClient, new HashMap<String, String>());

        final String path = "/path/to/endpoint";

        HostConfiguration hostConfiguration = new HostConfiguration();
        hostConfiguration.setHost("foobar.com", 3456, Protocol.getProtocol("http"));
        when(httpClient.getHostConfiguration()).thenReturn(hostConfiguration);
        when(httpClient.executeMethod(Matchers.<HttpMethod>any())).thenThrow(new IOException());

        client.postBytes(path, "body".getBytes("UTF-8"));
    }

    @Test
    public void testSpecifiedHeaderAdded() throws Exception {
        Map<String, String> headers = new HashMap<String, String>() {
            {
                put("testone", "valueone");
                put("testtwo", "valuetwo");
            }
        };

        ApacheHttpClient31BackedHttpClient client = new ApacheHttpClient31BackedHttpClient(httpClient, headers);

        mockResponse("body".getBytes(), 200, "blah");

        client.get("/path");
        client.postJson("/path", "lkdjla");
        client.postBytes("/path", "dkjafld".getBytes());

        verify(httpClient, times(3)).executeMethod(argThat(new HttpMethodHasHeadersMatcher(headers)));
    }

    private boolean pathMatches(HttpMethod method, String path) throws URIException {
        return method.getURI().getPath().equals(path);
    }

    private void mockResponse(byte[] body, int statusCode, String reason) throws IOException {
        final InputStream bodyInputStream = new ByteArrayInputStream(body);
        final StatusLine statusLine = new StatusLine("HTTP/1.1 " + statusCode + " " + reason + "\n");

        when(httpClient.executeMethod(argThat(new ArgumentMatcher<HttpMethod>() {
            @Override
            public boolean matches(Object o) {
                if (o instanceof HttpMethodBase) {
                    HttpMethodBase methodBase = (HttpMethodBase) o;

                    try {
                        Field responseStreamField = HttpMethodBase.class.getDeclaredField("responseStream");
                        responseStreamField.setAccessible(true);
                        responseStreamField.set(methodBase, bodyInputStream);
                        responseStreamField.setAccessible(false);

                        Field statusLineField = HttpMethodBase.class.getDeclaredField("statusLine");
                        statusLineField.setAccessible(true);
                        statusLineField.set(methodBase, statusLine);
                        statusLineField.setAccessible(false);

                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    return true;
                }
                return false;
            }
        }))).thenReturn(200);
    }

    private class HttpMethodHasHeadersMatcher extends ArgumentMatcher<HttpMethod> {

        private final Map<String, String> headers;

        private HttpMethodHasHeadersMatcher(Map<String, String> headers) {
            this.headers = headers;
        }

        @Override
        public boolean matches(Object argument) {
            if (!(argument instanceof HttpMethod)) {
                return false;
            }

            HttpMethod method = (HttpMethod) argument;
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                boolean found = false;
                for (Header header : method.getRequestHeaders()) {
                    if (header.getName().equals(entry.getKey()) && header.getValue().equals(entry.getValue())) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    return false;
                }
            }

            return true;
        }
    }

    private class PostMethodArgumentMatcher extends ArgumentMatcher<HttpMethod> {

        private final String expectedPath;
        private final byte[] expectedEntityBytes;
        private final String expectedContentTypeHeader;
        private final String expectedEntityContentType;

        private PostMethodArgumentMatcher(String expectedPath, byte[] expectedEntityBytes,
            String expectedContentTypeHeader, String expectedEntityContentType) {
            this.expectedPath = expectedPath;
            this.expectedEntityBytes = expectedEntityBytes;
            this.expectedContentTypeHeader = expectedContentTypeHeader;
            this.expectedEntityContentType = expectedEntityContentType;
        }

        @Override
        public boolean matches(Object o) {
            if (!(o instanceof PostMethod)) {
                return false;
            }

            PostMethod postMethod = (PostMethod) o;
            try {
                return pathMatches(postMethod, expectedPath) &&
                    hasContentTypeHeader(postMethod, expectedContentTypeHeader) &&
                    entityHasContentType(postMethod, expectedEntityContentType) &&
                    postEntityBodyMatches(postMethod, expectedEntityBytes);

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        private boolean postEntityBodyMatches(PostMethod postMethod, byte[] expected) throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            postMethod.getRequestEntity().writeRequest(out);

            return Arrays.equals(out.toByteArray(), expected);
        }

        private boolean hasContentTypeHeader(HttpMethod method, String contentType) {
            Header contentTypeHeader = method.getRequestHeader(Constants.CONTENT_TYPE_HEADER_NAME);
            return contentTypeHeader != null && contentTypeHeader.getValue().equals(contentType);
        }

        private boolean entityHasContentType(PostMethod postMethod, String contentType) {
            return postMethod.getRequestEntity().getContentType().equals(contentType);
        }

    }
}
