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
