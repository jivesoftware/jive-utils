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
package com.jivesoftware.os.server.http.jetty.jersey.server.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Resource {

    private static final Logger log = LoggerFactory.getLogger(Resource.class);

    private final File rootDir;

    private List<String> welcomeFiles = new ArrayList<>(Arrays.asList("index.html"));
    private boolean allowDirectoryListing = true;
    private String context = "/static";
    private List<String> resourcePaths = new ArrayList<>();

    public Resource(File rootDir) {
        this.rootDir = rootDir;
    }

    public Resource addResourcePath(String path) {
        resourcePaths.add(path);
        return this;
    }

    public Resource setWelcomeFiles(List<String> welcomeFiles) {
        this.welcomeFiles = welcomeFiles;
        return this;
    }

    public Resource setDirectoryListingAllowed(boolean allowDirectoryListing) {
        this.allowDirectoryListing = allowDirectoryListing;
        return this;
    }

    public Resource setContext(String context) {
        this.context = context;
        return this;
    }

    public String getContext() {
        return context;
    }

    public ResourceHandler getResourceHandler() {
        ResourceHandler handler = new ResourceHandler();

        String resourceBase = null;

        for (String path : resourcePaths) {
            File dir = new File(rootDir, path);
            if (dir.isDirectory() && dir.list().length > 0) {
                resourceBase = dir.getAbsolutePath();
                break;
            }
        }

        if (resourceBase == null) {
            throw new IllegalStateException("Could not find resourceBase for context " + this.context);
        }

        log.info("Assigning " + resourceBase + " to context " + this.context);
        handler.setResourceBase(resourceBase);
        handler.setWelcomeFiles(welcomeFiles.toArray(new String[welcomeFiles.size()]));
        handler.setDirectoriesListed(allowDirectoryListing);
        handler.setCacheControl("max-age=21600"); //6hrs
        handler.setEtags(true);

        return handler;
    }
}
