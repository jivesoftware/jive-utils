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
import java.util.List;

/**
 *
 */
public class JsonLogLevels {

    private final String tenantId;
    private final List<JsonLogLevel> logLevels;

    @JsonCreator
    public JsonLogLevels(@JsonProperty(value = "tenantId") String tenantId,
        @JsonProperty(value = "logLevels") List<JsonLogLevel> logLevels) {

        this.tenantId = tenantId;
        this.logLevels = logLevels;
    }

    public String getTenantId() {
        return tenantId;
    }

    public List<JsonLogLevel> getLogLevels() {
        return logLevels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JsonLogLevels that = (JsonLogLevels) o;

        if (logLevels != null ? !logLevels.equals(that.logLevels) : that.logLevels != null) {
            return false;
        }
        if (tenantId != null ? !tenantId.equals(that.tenantId) : that.tenantId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = tenantId != null ? tenantId.hashCode() : 0;
        result = 31 * result + (logLevels != null ? logLevels.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JsonLogLevels{" + "tenantId=" + tenantId + ", logLevels=" + logLevels + '}';
    }
}
