/*
 * Created: 10/2/12 by brad.jordan
 * Copyright (C) 1999-2012 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
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
