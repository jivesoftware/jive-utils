/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.base.util;

/**
 *
 * @author jonathan
 */
public class UtilPushback {

    public static long pushedback = 0;
    private static Runtime runtime = Runtime.getRuntime();

    public static void pushback(String name, double memoryLoadMax) {
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        double memoryLoad = (double) (total - free) / (double) total;
        if (memoryLoad > memoryLoadMax) {
            pushedback++;
            // could retard progress by sleeping thread
            // for now we will throw an exception
            throw new PushbackException("Service isn't keeping UP!");
        }
    }

    public static void queueDepthPushable(String name, long size, long pushbackAtQueueSize) {
        if (pushbackAtQueueSize > 0 && size > pushbackAtQueueSize) {
            pushedback++;
            throw new PushbackException("Unable to append because queue=" + name + " is at its pushback capacity of " + pushbackAtQueueSize);
        }
    }
}
