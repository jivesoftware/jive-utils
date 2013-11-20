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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CustomSecureProtocolSocketFactoryTest {

    private SSLSocketFactory sslSocketFactory;

    private CustomSecureProtocolSocketFactory customSecureProtocolSocketFactory;

    @BeforeMethod
    public void setUp() throws Exception {
        sslSocketFactory = mock(SSLSocketFactory.class);

        customSecureProtocolSocketFactory = new CustomSecureProtocolSocketFactory(sslSocketFactory);
    }

    @Test
    public void testCreateSocket() throws Exception {
        customSecureProtocolSocketFactory.createSocket("host", 111);

        verify(sslSocketFactory).createSocket("host", 111);
    }

    @Test
    public void testCreateSocketWithLocal() throws Exception {
        customSecureProtocolSocketFactory.createSocket("host", 333, InetAddress.getLocalHost(), 8765);

        verify(sslSocketFactory).createSocket("host", 333, InetAddress.getLocalHost(), 8765);
    }

    @Test
    public void testCreateSocketLocalWithTimeout() throws Exception {
        HttpConnectionParams params = mock(HttpConnectionParams.class);
        Socket socket = mock(Socket.class);

        when(params.getConnectionTimeout()).thenReturn(788);
        when(params.getSoTimeout()).thenReturn(789);
        when(sslSocketFactory.createSocket())
            .thenReturn(socket);

        customSecureProtocolSocketFactory.createSocket("host", 444, InetAddress.getLocalHost(), 8765, params);

        verify(sslSocketFactory).createSocket();
        verify(socket).setSoTimeout(789);
        verify(socket).connect(new InetSocketAddress("host", 444), 788);
    }
}
