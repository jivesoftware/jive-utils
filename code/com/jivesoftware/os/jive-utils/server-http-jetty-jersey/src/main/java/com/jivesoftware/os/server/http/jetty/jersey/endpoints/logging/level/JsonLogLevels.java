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
