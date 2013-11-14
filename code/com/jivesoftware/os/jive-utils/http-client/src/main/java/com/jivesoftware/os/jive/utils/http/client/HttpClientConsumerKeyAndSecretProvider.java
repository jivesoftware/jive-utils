/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.http.client;

/**
 *
 */
public interface HttpClientConsumerKeyAndSecretProvider {

    /**
     *
     * @param serviceName
     * @return
     */
    String getConsumerKey(String serviceName);

    /**
     *
     * @param serviceName
     * @return
     */
    String getConsumerSecret(String serviceName);
}
