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

import java.text.DecimalFormat;

/**
 * statics to covert stuff to more humanly readable
 *
 * @author jonathan
 */
public class UtilHuman {

    static final DecimalFormat df = new DecimalFormat("0.0");

    private UtilHuman() {
    }

    public static String ram(Double _bytes) {
        if (_bytes == null) {
            return "0";
        }
        return ram((long) (double) _bytes);
    }

    public static String ram(long _bytes) {
        if (_bytes < 0) {
            return "0";
        }
        if (_bytes < 1024L) {
            return _bytes + "b";
        }
        if (_bytes < (1024L * 1024L)) {
            return (_bytes / (1024L)) + "kb";
        }
        if (_bytes < (1024L * 1024L * 1024L)) {
            return df.format((double) _bytes / (double) (1024L * 1024L)) + "mb";
        }
        if (_bytes < (1024L * 1024L * 1024L * 1024L)) {
            return df.format(((double) _bytes / (double) (1024L * 1024L * 1024L))) + "gb";
        }
        if (_bytes < (1024L * 1024L * 1024L * 1024L * 1024L)) {
            return df.format(((double) _bytes / (double) (1024L * 1024L * 1024L * 1024L))) + "tb";
        }
        if (_bytes < (1024L * 1024L * 1024L * 1024L * 1024L * 1024L)) {
            return df.format(((double) _bytes / (double) (1024L * 1024L * 1024L * 1024L * 1024L))) + "pb";
        }
        return "" + _bytes;
    }
}
