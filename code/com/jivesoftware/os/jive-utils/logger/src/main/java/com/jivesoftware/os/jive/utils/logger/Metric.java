/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-2012 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.logger;

import java.util.Date;
import java.util.Map;

public interface Metric {

    public Metric put(String key, String value);

    public Metric put(String key, Integer value);

    public Metric put(String key, Long value);

    public Metric put(String key, Boolean value);

    public Metric put(String key, String[] value);

    public Metric put(String key, Map<String, String> value);

    public Metric put(String key, Iterable<String> value);

    public Metric put(String key, Date value);

    /**
     * Allows plain pojos to be added to the properties map. Care should be taken here because these events are eventually serialized (to json at the moment),
     * and you don't want to dump a huge object graph. When used with the jive service-logging-http durable logger, then the only properties on the pojos that
     * are serialized are those that specifically are annotated with an
     *
     * @JsonProperty annotation.
     */
    public Metric putPojo(String key, Object pojo);

    public void send();

    public void send(MetricEventWriter writer);
    public static final Metric NULL_METRIC = new Metric() {
        @Override
        public Metric put(String key, String value) {
            return this;
        }

        @Override
        public Metric put(String key, Integer value) {
            return this;
        }

        @Override
        public Metric put(String key, Long value) {
            return this;
        }

        @Override
        public Metric put(String key, Boolean value) {
            return this;
        }

        @Override
        public Metric put(String key, String[] value) {
            return this;
        }

        @Override
        public Metric put(String key, Map<String, String> value) {
            return this;
        }

        @Override
        public Metric put(String key, Iterable<String> value) {
            return this;
        }

        @Override
        public Metric put(String key, Date value) {
            return this;
        }

        @Override
        public Metric putPojo(String key, Object pojo) {
            return this;
        }

        @Override
        public void send() {
        }

        @Override
        public void send(MetricEventWriter writer) {
        }
    };
}
