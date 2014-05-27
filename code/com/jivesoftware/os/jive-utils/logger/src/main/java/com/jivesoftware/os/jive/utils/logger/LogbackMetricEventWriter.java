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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class LogbackMetricEventWriter implements MetricEventWriter {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public void writeEvent(MetricEvent event) {
        Logger logger = LoggerFactory.getLogger("jive-metrics." + event.getClassName() + "." + event.getMethodName());
        if (logger != null) {
            // Because we can only log strings, serialize to JSON now and add a flag to MDC
            // TODO: Encapsulate this in a static MetricEvent helper
            try {
                logger.info(jsonMapper.writeValueAsString(event));
                MDC.put(MetricEvent.class.toString(), MetricEvent.class.toString());
            } catch (JsonProcessingException e) {
            }
        }
    }
}