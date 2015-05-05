/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package org.merlin.config;

/**
 *
 */
public class ConfigurationRuntimeException extends RuntimeException {

    public ConfigurationRuntimeException(String msg, Throwable t) {
        super(msg, t);
    }

    public ConfigurationRuntimeException(String msg) {
        super(msg);
    }
}
