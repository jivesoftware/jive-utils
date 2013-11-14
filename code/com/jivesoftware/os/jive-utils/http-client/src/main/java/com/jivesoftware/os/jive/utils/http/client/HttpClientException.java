/*
 * $Revision: 109733 $
 * $Date: 2010-05-05 10:34:28 -0700 (Wed, 05 May 2010) $
 *
 * Copyright (C) 1999-2011 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.http.client;

public class HttpClientException extends Exception {

    public HttpClientException(String message) {
        super(message);
    }

    public HttpClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
