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
package com.jivesoftware.os.server.http.jetty.jersey.endpoints.logging.metric;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jivesoftware.os.jive.utils.logger.AtomicCounter;
import com.jivesoftware.os.jive.utils.logger.Counter;
import com.jivesoftware.os.jive.utils.logger.CountersAndTimers;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import com.jivesoftware.os.jive.utils.logger.Timer;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;

/**
 *
 * @author jonathan
 */
public class MetricsHelper {

    private final static MetricLogger logger = MetricLoggerFactory.getLogger();
    public static final MetricsHelper INSTANCE = new MetricsHelper();
    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.setVisibilityChecker(
            mapper.getVisibilityChecker()
                .withSetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY)
                .withGetterVisibility(JsonAutoDetect.Visibility.PUBLIC_ONLY)
            );
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private final LoggerMetrics counterMetrics = new LoggerMetrics();
    private final LoggerMetrics timerMetrics = new LoggerMetrics();

    public LoggerMetrics getCounters(String loggerName) {

        JVMMetrics.INSTANCE.logJMVMetrics();
        for (CountersAndTimers cat : get(loggerName)) {
            for (Entry<String, Counter> v : cat.getCounters()) {
                counterMetrics.put("counter>" + cat.getLoggerName() + ">" + v.getKey(),
                    v.getValue().getValue());
            }
            for (Entry<String, AtomicCounter> v : cat.getAtomicCounters()) {
                counterMetrics.put("atomicCounter>" + cat.getLoggerName() + ">" + v.getKey(),
                    v.getValue().getValue());
            }
        }
        return counterMetrics;
    }

    public LoggerMetrics getTimers(String loggerName) {

        for (CountersAndTimers cat : get(loggerName)) {
            for (Entry<String, Timer> v : cat.getTimers()) {
                timerMetrics.put("timer>" + cat.getLoggerName() + ">" + v.getKey() + ">min:millis",
                    (long) v.getValue().getMin());
                timerMetrics.put("timer>" + cat.getLoggerName() + ">" + v.getKey() + ">max:millis",
                    (long) v.getValue().getMax());
                timerMetrics.put("timer>" + cat.getLoggerName() + ">" + v.getKey() + ">mean:millis",
                    (long) v.getValue().getMean());
            }
        }
        return timerMetrics;
    }

    private Collection<CountersAndTimers> get(String loggerName) {
        if (loggerName == null || loggerName.length() == 0) {
            return CountersAndTimers.getAll();
        }
        return Arrays.asList(CountersAndTimers.getOrCreate(loggerName));
    }

    public static String toJson(Object instance) {
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        try {
            StringWriter sw = new StringWriter();
            mapper.writeValue(sw, instance);
            return sw.toString();
        } catch (Exception ex) {
            logger.error("Failed to create status!", ex);
            return "{}";
        }
    }
}
