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
package com.jivesoftware.os.server.http.health.check;

public class HealthCheckResponseImpl implements HealthCheckResponse {

    private final String checkName;
    private final boolean isHealthy;
    private final String message;

    public HealthCheckResponseImpl(String checkName, boolean isHealthy, String message) {
        this.checkName = checkName;
        this.isHealthy = isHealthy;
        this.message = message;
    }

    @Override
    public String getCheckName() {
        return checkName;
    }

    @Override
    public boolean isHealthy() {
        return isHealthy;
    }

    @Override
    public String getStatusMessage() {
        return message;
    }

}
