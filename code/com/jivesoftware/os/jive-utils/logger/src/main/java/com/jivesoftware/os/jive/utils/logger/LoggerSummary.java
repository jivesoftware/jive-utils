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
package com.jivesoftware.os.jive.utils.logger;

import com.jivesoftware.os.jive.utils.base.util.ISO8601DateFormat;
import com.jivesoftware.os.jive.utils.base.util.LastN;

import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author jonathan
 */
public class LoggerSummary {

    private static final ISO8601DateFormat DATE_FORMAT = new ISO8601DateFormat(TimeZone.getTimeZone("GMT"));
    public static LoggerSummary INSTANCE = new LoggerSummary();
    public static LoggerSummary INSTANCE_EXTERNAL_INTERACTIONS = new LoggerSummary();
    public long infos;
    public LastN<String> lastNInfos = new LastN<String>(new String[10]) {
        @Override
        public void add(String t) {
            super.add(DATE_FORMAT.format(new Date()) + " " + t);
        }
    };
    public long debugs;
    public long traces;
    public long warns;
    public LastN<String> lastNWarns = new LastN<String>(new String[10]) {
        @Override
        public void add(String t) {
            super.add(DATE_FORMAT.format(new Date()) + " " + t);
        }
    };
    public long errors;
    public LastN<String> lastNErrors = new LastN<String>(new String[10]) {
        @Override
        public void add(String t) {
            super.add(DATE_FORMAT.format(new Date()) + " " + t);
        }
    };

    public void reset() {
        lastNInfos = new LastN<>(new String[10]);
        infos = 0;
        debugs = 0;
        traces = 0;
        warns = 0;
        errors = 0;
    }
}
