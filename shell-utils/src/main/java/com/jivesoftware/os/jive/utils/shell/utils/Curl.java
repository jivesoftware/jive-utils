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
