/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.server.http.jetty.jersey.server;

import com.jivesoftware.os.jive.utils.base.service.ServiceHandle;
import com.jivesoftware.os.server.http.jetty.jersey.server.util.Resource;

public class InitializeRestfulServer {

    private final RestfulServer server;

    public InitializeRestfulServer(
            int port,
            String applicationName,
            int maxNumberOfThreads,
            int maxQueuedRequests) {
        server = new RestfulServer(port, applicationName, maxNumberOfThreads, maxQueuedRequests);
    }

    public InitializeRestfulServer addContextHandler(String context, HasServletContextHandler contextHandler) {
        server.addContextHandler(context, contextHandler);
        return this;
    }

    public InitializeRestfulServer addContextHandler(String context, JerseyEndpoints contextHandler) {
        contextHandler.humanReadableJson();
        server.addContextHandler(context, contextHandler);
        return this;
    }

    public InitializeRestfulServer addResource(Resource resource) {
        addContextHandler(resource.getContext(), new StaticEndpoint(resource));
        return this;
    }

    public ServiceHandle build() {
        return server;
    }
}
