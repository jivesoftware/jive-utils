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
package com.jivesoftware.os.jive.utils.health;

public class HealthCheckResponseImpl implements HealthCheckResponse {

    private final String name;
    private final double health;
    private final String status;
    private final String description;
    private final String resolution;
    private final long timestamp;

    public HealthCheckResponseImpl(String name,
        double health,
        String status,
        String description,
        String resolution,
        long timestamp) {
        this.name = name;
        this.health = health;
        this.status = status;
        this.description = description;
        this.resolution = resolution;
        this.timestamp = timestamp;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getHealth() {
        return health;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getResolution() {
        return resolution;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

}
