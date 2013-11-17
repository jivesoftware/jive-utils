/*
 * Created: 7/9/12 by brad.jordan
 * Copyright (C) 1999-2012 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.http.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.StatusLine;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test
public class ApacheHttpClient31BackedHttpClientOAuthTest {

    private ApacheHttpClient31BackedHttpClient oAuthHttpClient;
    private org.apache.commons.httpclient.HttpClient httpClient;

    @BeforeMethod
    public void setup() throws Exception {
        httpClient = mock(org.apache.commons.httpclient.HttpClient.class);
        when(httpClient.getHostConfiguration()).thenReturn(new HostConfiguration());
        oAuthHttpClient = new ApacheHttpClient31BackedHttpClient(httpClient, new HashMap<String, String>());
    }

    public void testConsumerIsNullUnlessConsumerTokensAreSet() {
        Assert.assertNull(oAuthHttpClient.getConsumer());
    }

    public void testConsumerNotNullWhenConsumerTokensAreSet() {
        oAuthHttpClient.setConsumerTokens("123", "456");
        Assert.assertNotNull(oAuthHttpClient.getConsumer());

    }

    public void testAuthorizationHeaderNotPresentWhenConsumerTokensNotSet() throws Exception {
        String path = "http://foo.bar.baz/foo";
        oAuthHttpClient = new ApacheHttpClient31BackedHttpClient(httpClient, new HashMap<String, String>());

        mockResponse("body".getBytes(), 200, "blah");

        oAuthHttpClient.get(path);
        ArgumentCaptor<HttpMethod> argument = ArgumentCaptor.forClass(HttpMethod.class);
        verify(httpClient).executeMethod(argument.capture());

        Assert.assertNull(argument.getValue().getRequestHeader("Authorization"));
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
}
