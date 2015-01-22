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

import com.google.inject.Singleton;
import com.jivesoftware.os.jive.utils.base.interfaces.CallbackStream;
import com.jivesoftware.os.jive.utils.jaxrs.util.ResponseHelper;
import com.jivesoftware.os.mlogger.core.CountersAndTimers;
import com.jivesoftware.os.mlogger.core.LoggerSummary;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Singleton
@Path("/logging/metric")
public class LogMetricRestfulEndpoints {

    @GET
    @Path("/listCounters")
    public Response listCounters(@QueryParam("logger") @DefaultValue("ALL") String loggerName, @QueryParam("callback") @DefaultValue("") String callback) {

        try {
            if (loggerName.equals("ALL")) {
                loggerName = "";
            }
            final Metrics metrics = new Metrics();
            MetricsHelper.INSTANCE.getCounters(loggerName).getAll(new CallbackStream<Entry<String, Long>>() {

                @Override
                public Entry<String, Long> callback(Entry<String, Long> v) throws Exception {
                    if (v != null) {
                        metrics.metrics.add(new KeyAndMetric(v.getKey(), v.getValue()));
                    }
                    return v;
                }
            });
            if (callback.length() > 0) {
                return ResponseHelper.INSTANCE.jsonpResponse(callback, metrics);
            } else {
                return ResponseHelper.INSTANCE.jsonResponse(metrics);
            }

        } catch (Exception ex) {
            return ResponseHelper.INSTANCE.errorResponse("Failed to list counters.", ex);
        }

    }

    @GET
    @Path("/listTimers")
    public Response listTimers(@QueryParam("logger") @DefaultValue("ALL") String loggerName, @QueryParam("callback") @DefaultValue("") String callback) {
        try {
            if (loggerName.equals("ALL")) {
                loggerName = "";
            }

            final Metrics metrics = new Metrics();
            MetricsHelper.INSTANCE.getTimers(loggerName).getAll(new CallbackStream<Entry<String, Long>>() {

                @Override
                public Entry<String, Long> callback(Entry<String, Long> v) throws Exception {
                    if (v != null) {
                        metrics.metrics.add(new KeyAndMetric(v.getKey(), v.getValue()));
                    }
                    return v;
                }
            });
            if (callback.length() > 0) {
                return ResponseHelper.INSTANCE.jsonpResponse(callback, metrics);
            } else {
                return ResponseHelper.INSTANCE.jsonResponse(metrics);
            }
        } catch (Exception ex) {
            return ResponseHelper.INSTANCE.errorResponse("Failed to list timers.", ex);
        }
    }

    @GET
    @Path("/resetCounter")
    public String resetCounter() {
        CountersAndTimers.resetAll();
        LoggerSummary.INSTANCE.reset();
        return "counter were reset.";
    }

    static class Metrics {

        public List<KeyAndMetric> metrics = new LinkedList<>();

        public Metrics() {
        }
    }

    static class KeyAndMetric {

        public String key;
        public double metric;

        public KeyAndMetric() {
        }

        public KeyAndMetric(String key, double metric) {
            this.key = key;
            this.metric = metric;
        }
    }

}
