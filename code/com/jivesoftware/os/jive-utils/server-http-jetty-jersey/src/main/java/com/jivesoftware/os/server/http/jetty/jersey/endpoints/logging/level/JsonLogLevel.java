/*
 * $Revision: 142270 $
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.server.http.jetty.jersey.endpoints.logging.level;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class JsonLogLevel {

    private final String loggerName;
    private final String loggerLevel;

    @JsonCreator
    public JsonLogLevel(
        @JsonProperty(value = "loggerName") String loggerName,
        @JsonProperty(value = "loggerLevel") String loggerLevel) {
        this.loggerName = loggerName;
        this.loggerLevel = loggerLevel;
    }

    public String getLoggerLevel() {
        return loggerLevel;
    }

    public String getLoggerName() {
        return loggerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JsonLogLevel that = (JsonLogLevel) o;

        if (loggerLevel != null ? !loggerLevel.equals(that.loggerLevel) : that.loggerLevel != null) {
            return false;
        }
        if (loggerName != null ? !loggerName.equals(that.loggerName) : that.loggerName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = loggerName != null ? loggerName.hashCode() : 0;
        result = 31 * result + (loggerLevel != null ? loggerLevel.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JsonLogLevel{" + "loggerName=" + loggerName + ", loggerLevel=" + loggerLevel + '}';
    }
}
