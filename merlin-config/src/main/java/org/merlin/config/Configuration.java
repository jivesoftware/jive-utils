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

/**
 *
 */
public interface Configuration {

    public void setProperty(String propertyName, String value);

    public void setProperty(String propertyName, String value, String descriptions);

    public String getProperty(String propertyName);

    public String getDescription(String propertyName);

    public Collection<String> getPropertyNames();

    public void clearProperty(String propertyName);

    public void clear();
}
