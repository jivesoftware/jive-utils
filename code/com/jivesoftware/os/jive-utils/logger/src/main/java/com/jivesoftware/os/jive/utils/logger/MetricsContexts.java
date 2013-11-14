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

import java.util.HashMap;
import java.util.Map;

public class MetricsContexts {

    private static Map<String, MetricsContext> contextRegistry = new HashMap<>();

    static {
        contextRegistry.put("jvm", new JvmMetricsContext());
    }

    public synchronized static void register(String name, MetricsContext context) {
        if (contextRegistry.containsKey(name)) {
            throw new IllegalStateException("MetricsContext already registered under name '" + name + "'.");
        }
        Map<String, MetricsContext> updated = new HashMap<>();
        updated.putAll(contextRegistry);
        updated.put(name, context);

        contextRegistry = updated;
    }

    public synchronized static void unregister(String name, MetricsContext context) {
        if (contextRegistry.get(name) == context) {
            Map<String, MetricsContext> updated = new HashMap<>();
            updated.putAll(contextRegistry);
            updated.remove(name);

            contextRegistry = updated;
        }
    }

    public static Map<String, Map<String, String>> report() {
        Map<String, Map<String, String>> report = new HashMap<>();
        for (Map.Entry<String, MetricsContext> entry : contextRegistry.entrySet()) {
            Map<String, String> entryReport = entry.getValue().report();
            if (entryReport != null && !entryReport.isEmpty()) {
                report.put(entry.getKey(), entryReport);
            }
        }
        return report;
    }
}
