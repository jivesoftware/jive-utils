/*
 * $Revision: 142270 $
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.logger;

import com.jivesoftware.os.jive.utils.base.util.LastN;
import java.util.Date;
import java.util.TimeZone;
import org.apache.log4j.helpers.ISO8601DateFormat;

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
