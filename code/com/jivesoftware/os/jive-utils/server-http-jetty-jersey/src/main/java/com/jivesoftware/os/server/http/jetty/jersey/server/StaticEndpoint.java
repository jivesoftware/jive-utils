/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.server.http.jetty.jersey.server;

import com.jivesoftware.os.server.http.jetty.jersey.server.util.Resource;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;

public class StaticEndpoint implements HasServletContextHandler {

    private Resource resource;

    public StaticEndpoint(Resource resource) {
        this.resource = resource;
    }

    @Override
    public Handler getHandler(Server server, String context, String applicationName) {
        ContextHandler handler = new ContextHandler();
        handler.setContextPath(context);
        handler.setHandler(resource.getResourceHandler());
        handler.setDisplayName(applicationName);
        return handler;
    }
}
