/*
 * $Revision: 109733 $
 * $Date: 2010-05-05 10:34:28 -0700 (Wed, 05 May 2010) $
 *
 * Copyright (C) 1999-2011 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.http.client;

public interface HttpClientFactory {

    /**
     *
     * @param host
     * @param port
     * @return
     */
    HttpClient createClient(String host, int port);

}
