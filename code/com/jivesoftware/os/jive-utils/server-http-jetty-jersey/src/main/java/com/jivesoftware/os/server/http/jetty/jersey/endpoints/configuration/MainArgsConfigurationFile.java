/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */

package com.jivesoftware.os.server.http.jetty.jersey.endpoints.configuration;

/**
 *
 */
public class MainArgsConfigurationFile {
    private final String configFile;

    public MainArgsConfigurationFile(String configFile) {
        this.configFile = configFile;
    }

    public String getConfigFile() {
        return configFile;
    }

}
