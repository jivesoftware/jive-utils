/*
 * Created: 10/2/12 by brad.jordan
 * Copyright (C) 1999-2012 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.logger;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class MetricEvent {

    public enum InclusionExclusionHandling {

        EXCLUSION_ONLY, INCLUSION_ONLY, RESOLVE_BOTH
    }
    private static final String METRICS_SCHEMA_VERSION = "1.0";
    private static final AtomicLong ID_SEQ = new AtomicLong(0L);
    private String name;
    private Map<String, Object> properties;
    private final long timestamp;
    private final long seqId;
    private final String uuid;
    private final String className;
    private final String methodName;
    private final Integer lineNumber;
    private Map<String, Map<String, String>> context;
    private static boolean enabled = Boolean.getBoolean("metrics.enabled"); //off by default
    private static HashSet<String> excludedEventsImmutable = new HashSet<>();
    private static HashSet<String> includedEventsImmutable = new HashSet<>();
    private static InclusionExclusionHandling inclusionExclusionHandling = InclusionExclusionHandling.RESOLVE_BOTH;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        MetricEvent.enabled = enabled;
    }

    public synchronized static void addToExclusionList(String item) {
        HashSet<String> copy = new HashSet<>(excludedEventsImmutable);
        copy.add(item);
        excludedEventsImmutable = copy;
    }

    public synchronized static void removeFromExclusionList(String item) {
        HashSet<String> copy = new HashSet<>(excludedEventsImmutable);
        copy.remove(item);
        excludedEventsImmutable = copy;
    }

    public synchronized static void addToExclusionList(Set<String> items) {
        HashSet<String> copy = new HashSet<>(items.size() + excludedEventsImmutable.size());
        copy.addAll(excludedEventsImmutable);
        copy.addAll(items);
        excludedEventsImmutable = copy;
    }

    public synchronized static void clearExclusionList() {
        excludedEventsImmutable = new HashSet<>();
    }

    public static Set<String> getExclusionList() {
        return new HashSet<>(excludedEventsImmutable);
    }

    public static boolean isExcluded(String name) {
        return excludedEventsImmutable.contains(name);
    }

    public synchronized static void addToInclusionList(String item) {
        HashSet<String> copy = new HashSet<>(includedEventsImmutable);
        copy.add(item);
        includedEventsImmutable = copy;
    }

    public synchronized static void removeFromInclusionList(String item) {
        HashSet<String> copy = new HashSet<>(includedEventsImmutable);
        copy.remove(item);
        includedEventsImmutable = copy;
    }

    public synchronized static void addToInclusionList(Set<String> items) {
        HashSet<String> copy = new HashSet<>(items.size() + includedEventsImmutable.size());
        copy.addAll(includedEventsImmutable);
        copy.addAll(items);
        includedEventsImmutable = copy;
    }

    public synchronized static void clearInclusionList() {
        includedEventsImmutable = new HashSet<>();
    }

    public static Set<String> getInclusionList() {
        return new HashSet<>(includedEventsImmutable);
    }

    public static boolean isIncluded(String name) {
        return includedEventsImmutable.contains(name);
    }

    public static InclusionExclusionHandling getInclusionExclusionHandling() {
        return inclusionExclusionHandling;
    }

    public static void setInclusionExclusionHandling(InclusionExclusionHandling inclusionExclusionHandling) {
        MetricEvent.inclusionExclusionHandling = inclusionExclusionHandling;
    }

    private MetricEvent(String name, Map<String, Object> properties, Map<String, Map<String, String>> contextReport,
            StackTraceElement stack) {
        this(name, properties, contextReport, UUID.randomUUID().toString(),
                System.currentTimeMillis(), stack.getClassName(), stack.getMethodName(), stack.getLineNumber(), ID_SEQ.incrementAndGet());
    }

    private MetricEvent(String name, Map<String, Object> properties, Map<String, Map<String, String>> contextReport,
            String uuid, long timestamp, String className, String methodName, Integer lineNumber, Long seqId) {
        this.name = name;
        if (properties != null && !properties.isEmpty()) {
            this.properties = properties;
        }
        this.timestamp = timestamp;
        this.uuid = uuid;
        this.className = className;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
        this.context = contextReport;
        this.seqId = seqId;
    }

    public String getVersion() {
        return METRICS_SCHEMA_VERSION;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getProperties() {
        if (properties == null) {
            return null;
        }
        return Collections.unmodifiableMap(properties);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getUuid() {
        return uuid;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public Map<String, Map<String, String>> getContext() {
        return context;
    }

    public Map<String, String> getContext(String name) {
        return context.get(name);
    }

    public long getSeqId() {
        return seqId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MetricEvent that = (MetricEvent) o;

        if (!uuid.equals(that.uuid)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    public static Metric metric(String eventName) {
        Metric retVal = Metric.NULL_METRIC;

        // this is the case for most services so get this out of the way.
        if (!isEnabled()) {
            return retVal;
        }

        // Metrics is enabled so based on how handling is set decide how to use the inclusion and exclusion lists
        // EXCLUSION_ONLY means that the contents of the inclusion list don't matter, i.e. never log it if it's in
        // the exclusion list
        // INCLUSION_ONLY means that the contents of the exclusion list don't matter, i.e. always log it if it's in
        // the inclusion list
        // RESOLVE_BOTH means use both lists. If someone has biffed the config and an item is in both lists it should
        // not be logged. Having an item in both. Changing the biff handling to "item in both should be logged"
        // is identical to setting INCLUSION_ONLY.
        InclusionExclusionHandling handling = getInclusionExclusionHandling();
        switch (handling) {
            case EXCLUSION_ONLY:
                if (!excludedEventsImmutable.contains(eventName)) {
                    retVal = new MetricImpl(eventName);
                }
                break;
            case INCLUSION_ONLY:
                if (includedEventsImmutable.contains(eventName)) {
                    retVal = new MetricImpl(eventName);
                }
                break;
            case RESOLVE_BOTH:
                if (!excludedEventsImmutable.contains(eventName) && includedEventsImmutable.contains(eventName)) {
                    retVal = new MetricImpl(eventName);
                }
                break;
            default:
                retVal = Metric.NULL_METRIC;
                break;
        }

        return retVal;
    }

    @Override
    public String toString() {
        return "MetricEvent{"
                + "name='" + name + '\''
                + ", properties=" + properties
                + ", timestamp=" + timestamp
                + ", seqId=" + seqId
                + ", uuid='" + uuid + '\''
                + ", className='" + className + '\''
                + ", methodName='" + methodName + '\''
                + ", lineNumber=" + lineNumber
                + ", context=" + context
                + '}';
    }

    private static final class MetricImpl implements Metric {

        private static final MetricEventWriter DEFAULT_METRIC_WRITER = new Log4jMetricEventWriter();
        private String name;
        private Map<String, Object> properties;

        public MetricImpl(String name) {
            this.name = name;
            this.properties = new HashMap<>();
        }

        public MetricImpl put(String key, String value) {
            return putObject(key, value);
        }

        public MetricImpl put(String key, Integer value) {
            return putObject(key, value);
        }

        public MetricImpl put(String key, Long value) {
            return putObject(key, value);
        }

        public MetricImpl put(String key, Boolean value) {
            return putObject(key, value);
        }

        public MetricImpl put(String key, String[] value) {
            if (value == null || value.length == 0) {
                return this;
            }
            return putObject(key, value);
        }

        public MetricImpl put(String key, Map<String, String> value) {
            if (value == null || value.isEmpty()) {
                return this;
            }
            return putObject(key, value);
        }

        public Metric put(String key, Iterable<String> value) {
            return putObject(key, value);
        }

        @Override
        public Metric put(String key, Date value) {
            return putObject(key, value);
        }

        @Override
        public Metric putPojo(String key, Object pojo) {
            return putObject(key, pojo);
        }

        private MetricImpl putObject(String key, Object value) {
            if (value != null) {
                this.properties.put(key, value);
            }
            return this;
        }

        @Override
        public void send() {
            send(DEFAULT_METRIC_WRITER, Thread.currentThread().getStackTrace()[2]);
        }

        @Override
        public void send(MetricEventWriter writer) {
            send(writer, Thread.currentThread().getStackTrace()[2]);
        }

        private void send(MetricEventWriter writer, StackTraceElement stack) {
            writer.writeEvent(new MetricEvent(name, properties, MetricsContexts.report(), stack));
        }
    }
}
