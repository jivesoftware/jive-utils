/*
 * Copyright 2013 Jive Software, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
        T[] clone = lastN.clone();
        for (int j = 0; j < clone.length; j++) {
            clone[j] = lastN[j + i % clone.length];
        }
        return clone;
    }

    public void clear(T fill) {
        Arrays.fill(lastN, fill);
    }
}
