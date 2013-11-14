/*
 * $Revision: 109733 $
 * $Date: 2010-05-05 10:34:28 -0700 (Wed, 05 May 2010) $
 *
 * Copyright (C) 1999-2011 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.http.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

public class CustomSecureProtocolSocketFactory implements ProtocolSocketFactory {

    private final SSLSocketFactory sslSocketFactory;

    public CustomSecureProtocolSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort)
        throws IOException {
        return sslSocketFactory.createSocket(host, port, localAddress, localPort);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort, HttpConnectionParams params)
        throws IOException {

        Socket socket = sslSocketFactory.createSocket();

        if (localAddress != null && port > 0) {
            socket.bind(new InetSocketAddress(localAddress, localPort));
        }

        int timeout = params.getSoTimeout();
        if (timeout > 0) {
            socket.setSoTimeout(timeout);
        }

        socket.connect(new InetSocketAddress(host, port), params.getConnectionTimeout());

        return socket;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return sslSocketFactory.createSocket(host, port);
    }
}
