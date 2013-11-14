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
