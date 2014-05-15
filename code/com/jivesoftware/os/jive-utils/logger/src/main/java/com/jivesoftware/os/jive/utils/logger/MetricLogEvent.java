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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;

public class MetricLogEvent extends LoggingEvent {

    private final String[] tags;

    public MetricLogEvent(String fqnOfCategoryClass, Logger logger,
            Level level, String[] tags, String message, Throwable throwable) {
        super(fqnOfCategoryClass, logger, level, message, throwable, null);
        this.tags = tags;
    }

    public String[] getTags() {
        return tags;
    }
}
