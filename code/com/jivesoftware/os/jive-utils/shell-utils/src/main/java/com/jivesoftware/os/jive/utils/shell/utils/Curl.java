/*
 * $Revision: 142270 $
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.shell.utils;

import java.io.IOException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 *
 * @author jonathan
 */
public class Curl {

    public static Curl create() {
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        HttpClient client = new HttpClient(connectionManager);

        return new Curl(client);
    }
    private final HttpClient client;

    public Curl(HttpClient client) {
        this.client = client;
    }

    public String curl(String path) throws IOException {
        GetMethod method = new GetMethod(path);
        byte[] responseBody;
        StatusLine statusLine;
        try {
            client.executeMethod(method);

            responseBody = method.getResponseBody();
            statusLine = method.getStatusLine();
            if (statusLine.getStatusCode() == 200) {
                return new String(responseBody);
            } else {
                return null;
            }
        } finally {
            method.releaseConnection();
        }
    }
}
