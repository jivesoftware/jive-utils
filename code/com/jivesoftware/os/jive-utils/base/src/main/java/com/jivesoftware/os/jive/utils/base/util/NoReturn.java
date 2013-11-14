/*
 * $Revision: 142270 $
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.base.util;

/**
 * Used with Callable when you don't want to return anything.
 *
 * @author jonathan
 */
public class NoReturn {

    public static final NoReturn RETURN = new NoReturn();

    private NoReturn() {
    }
}
