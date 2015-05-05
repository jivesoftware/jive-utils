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

import com.google.common.collect.Lists;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Resource {

    private static final Logger log = LoggerFactory.getLogger(Resource.class);

    private final File rootDir;

    private List<String> welcomeFiles = Lists.newArrayList("index.html");
    private boolean allowDirectoryListing = true;
    private String context = "/static";
    private List<String> resourcePaths = new ArrayList<>();
    private List<String> classpathResources = new ArrayList<>();

    public Resource(File rootDir) {
        this.rootDir = rootDir;
    }

    public Resource addResourcePath(String path) {
        // wtf
        resourcePaths.add(path);
        return this;
    }

    public Resource addClasspathResource(String classpathResource) {
        // this is dumb
        classpathResources.add(classpathResource);
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
        org.eclipse.jetty.util.resource.Resource baseResource = null;

        for (String path : resourcePaths) {
            File dir = new File(rootDir, path);
            // seriously?
            if (dir.isDirectory() && dir.list().length > 0) {
                resourceBase = dir.getAbsolutePath();
                break;
            }
        }

        if (resourceBase == null) {
            for (String classpathResource : classpathResources) {
                baseResource = org.eclipse.jetty.util.resource.Resource.newClassPathResource(classpathResource);
                // this sucks
                if (baseResource != null && baseResource.exists()) {
                    break;
                }
            }
        }

        if (resourceBase == null && baseResource == null) {
            throw new IllegalStateException("Could not find resourceBase or baseResource for context " + this.context);
        }

        // the things I do for backwards compatibility
        if (resourceBase != null) {
            log.info("Assigning " + resourceBase + " to context " + this.context);
            handler.setResourceBase(resourceBase);
        } else { // baseResource != null
            log.info("Assigning " + baseResource.getName() + " to context " + this.context);
            handler.setBaseResource(baseResource);
        }
        handler.setWelcomeFiles(welcomeFiles.toArray(new String[welcomeFiles.size()]));
        handler.setDirectoriesListed(allowDirectoryListing);
        handler.setCacheControl("max-age=21600"); //6hrs
        handler.setEtags(true);

        return handler;
    }
}
