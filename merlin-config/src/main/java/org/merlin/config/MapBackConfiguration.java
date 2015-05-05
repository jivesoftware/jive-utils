/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package org.merlin.config;

import java.util.Collection;
import java.util.Map;

/**
 *
 */
public class MapBackConfiguration implements Configuration {

    private final Map<String, String> configuration;

    public MapBackConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
    }

    @Override
    public void setProperty(String key, String value) {
        this.setProperty(key, value, "");
    }

    @Override
    public void setProperty(String key, String value, String description) {
        configuration.put(key, value);
    }

    @Override
    public Collection<String> getPropertyNames() {
        return configuration.keySet();
    }

    @Override
    public String getDescription(String propertyName) {
        return "un-implemented";
    }

    @Override
    public String getProperty(String key) {
        Object got = configuration.get(key);
        if (got instanceof String) {
            return (String) got;
        }
        if (got != null) {
            return got.toString();
        }
        return null;
    }

    @Override
    public void clearProperty(String key) {
        configuration.remove(key);
    }

    @Override
    public void clear() {
        configuration.clear();
    }

    @Override
    public String toString() {
        return "MapBackConfiguration{" + "configuration=" + configuration + '}';
    }
}
