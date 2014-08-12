/*
 * Copyright 2014 Jive Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jivesoftware.os.jive.utils.http.client;

/**
 * Created with IntelliJ IDEA.
 * User: dovamir
 * Date: 2/6/14
 * Time: 11:06 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractHttpResponse {
    protected final int statusCode;
    protected final String statusReasonPhrase;

    public AbstractHttpResponse(int statusCode, String statusReasonPhrase) {
        this.statusCode = statusCode;
        this.statusReasonPhrase = statusReasonPhrase;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusReasonPhrase() {
        return statusReasonPhrase;
    }
}