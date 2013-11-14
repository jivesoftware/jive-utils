/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.server.http.jetty.jersey.server.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.handler.ResourceHandler;

public class Resource {
    private static final Logger log = Logger.getLogger(Resource.class);

    private List<String> welcomeFiles = new ArrayList<String>(Arrays.asList("index.html"));
    private boolean allowDirectoryListing = false;
    private String context = "/static";
    private String localRootDir = "../../../..";

    public Resource() {
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

        File rootDir = getProdRootDir();
        String fileName = this.welcomeFiles.get(0);

        File knownResource = findResourcesDir(rootDir, fileName);
        if (knownResource == null) {
            rootDir = getLocalRootDir();
            knownResource = findResourcesDir(rootDir, fileName);
        }
        if (knownResource == null) {
            knownResource = new File(System.getProperty("user.dir"));
            log.info("Unable to find static file " + fileName);
        }
        File knownResourceDir = knownResource.getParentFile();
        log.info("Assiging " + knownResourceDir.toString() + " to context " + this.context);
        handler.setResourceBase(knownResourceDir.getPath());
        handler.setWelcomeFiles(welcomeFiles.toArray(new String[welcomeFiles.size()]));
        handler.setDirectoriesListed(allowDirectoryListing);

        return handler;
    }

    private File getProdRootDir() {
        String userDir = System.getProperty("user.dir");
        File hack = new File(userDir);
        return hack;
    }

    private File getLocalRootDir() {
        return new File(System.getProperty("user.dir"), localRootDir).getAbsoluteFile();
    }

    public void setLocalRootDir(String localRootDir) {
        this.localRootDir = localRootDir;
    }

    private static File findResourcesDir(File rootDir, String fileName) {
        File[] files = rootDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                // exclude node_modules directory
                return !name.equals("node_modules");
            }
        });
        if (files != null) {
            List<File> directories = new ArrayList<File>(files.length);
            for (File file : files) {
                if (file.getName().equals(fileName)) {
                    return file;
                } else if (file.isDirectory()) {
                    directories.add(file);
                }
            }
            for (File directory : directories) {
                File file = findResourcesDir(directory, fileName);
                if (file != null) {
                    return file;
                }
            }
        }
        return null;
    }
}
