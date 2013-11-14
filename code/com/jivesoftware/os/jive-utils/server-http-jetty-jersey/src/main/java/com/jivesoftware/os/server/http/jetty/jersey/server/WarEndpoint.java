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
import org.eclipse.jetty.webapp.WebAppContext;

/**
 *
 */
public class WarEndpoint implements HasServletContextHandler {

    private final String pathToWar;

    public WarEndpoint(String pathToWar) {
        this.pathToWar = pathToWar;
    }

    @Override
    public Handler getHandler(Server server, String context, String applicationName) {
        WebAppContext webAppContext = new WebAppContext(pathToWar, context);
        if (applicationName != null) {
            webAppContext.setDisplayName(applicationName);
        }
        return webAppContext;
    }
}
