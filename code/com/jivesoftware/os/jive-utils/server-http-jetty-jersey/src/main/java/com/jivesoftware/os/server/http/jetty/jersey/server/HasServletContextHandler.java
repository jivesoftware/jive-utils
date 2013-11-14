/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.server.http.jetty.jersey.server;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;

/**
 *
 */
public interface HasServletContextHandler {

    Handler getHandler(Server server, String context, String applicationName);
}
