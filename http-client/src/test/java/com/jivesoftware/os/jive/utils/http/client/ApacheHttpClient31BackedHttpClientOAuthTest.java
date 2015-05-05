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
