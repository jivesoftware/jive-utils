/*
 * $Revision: 142270 $
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.base.service;

/**
 *
 * @author jonathan
 */
public interface ServiceHandle {

    public void start() throws Exception;

    public void stop() throws Exception;
}
