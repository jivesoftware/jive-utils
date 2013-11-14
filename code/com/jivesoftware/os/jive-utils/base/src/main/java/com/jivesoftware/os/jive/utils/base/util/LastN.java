/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.base.util;

import java.util.Arrays;

/**
 * Not thread safe
 *
 * @author jonathan
 */
public class LastN<T> {

    final private T[] lastN;
    private int i = 0;

    public LastN(T[] lastN) {
        this.lastN = lastN;
    }

    public void add(T t) {
        int stackCopy = i;
        lastN[stackCopy] = t;
        stackCopy++;
        if (stackCopy >= lastN.length) {
            stackCopy = 0;
        }
        i = stackCopy;
    }

    public T[] get() {
        return lastN;
    }

    public void clear(T fill) {
        Arrays.fill(lastN, fill);
    }
}
