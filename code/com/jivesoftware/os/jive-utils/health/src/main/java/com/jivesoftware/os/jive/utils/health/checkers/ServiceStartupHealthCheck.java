/*
 * Copyright 2015 Jive Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jivesoftware.os.jive.utils.health.checkers;

import com.jivesoftware.os.jive.utils.health.HealthCheck;
import com.jivesoftware.os.jive.utils.health.HealthCheckResponse;
import com.jivesoftware.os.jive.utils.health.HealthCheckResponseImpl;
import com.jivesoftware.os.mlogger.core.MetricLogger;
import com.jivesoftware.os.mlogger.core.MetricLoggerFactory;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author jonathan.colt
 */
public class ServiceStartupHealthCheck implements HealthCheck {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();
    private final AtomicBoolean healthy = new AtomicBoolean();
    private final StringBuilder messages = new StringBuilder();
    private final AtomicReference<String> lastMessage = new AtomicReference<>("");

    @Override
    public HealthCheckResponse checkHealth() throws Exception {
        if (healthy.get()) {
            return new HealthCheckResponseImpl("service>startup", 1.0, "Running", "Service startup was successful.", "", System.currentTimeMillis());
        } else {
            return new HealthCheckResponse() {

                @Override
                public String getName() {
                    return "service>startup";
                }

                @Override
                public double getHealth() {
                    return 0;
                }

                @Override
                public String getStatus() {
                    return lastMessage.get();
                }

                @Override
                public String getDescription() {
                    return messages.toString();
                }

                @Override
                public String getResolution() {
                    return "Look into configuration or connectivity issues.";
                }

                @Override
                public long getTimestamp() {
                    return System.currentTimeMillis();
                }
            };
        }
    }

    public void info(String message, Throwable t) {
        if (t != null) {
            LOG.info(message, t);
        } else {
            LOG.info(message);
        }
        lastMessage.set(message);
        messages.append(message).append("\n");
        if (t != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            messages.append(pw.toString());
        }
    }

    public void success() {
        healthy.compareAndSet(false, true);
        messages.delete(0, messages.length());
    }
}
