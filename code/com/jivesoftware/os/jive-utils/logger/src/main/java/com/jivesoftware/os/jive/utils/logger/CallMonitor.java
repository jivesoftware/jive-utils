/*
 * $Revision: 142270 $
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.logger;

import java.util.concurrent.Callable;

/**
 * Interface to generalize tracking info around a method call.
 *
 * @author jonathan
 */
public interface CallMonitor {

    public <V> V call(Callable<V> callable) throws Exception;
}
