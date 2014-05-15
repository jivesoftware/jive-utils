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
package com.jivesoftware.os.server.http.jetty.jersey.endpoints.logging.level;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.google.inject.Singleton;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.LoggerFactory;

@Singleton
@Path("/logging")
public class LogLevelRestEndpoints {

    private static final MetricLogger log = MetricLoggerFactory.getLogger();

    @GET
    @Path("/listLogLevels")
    public Response listLogLevels() {
        StringBuilder sb = new StringBuilder();
        for (JsonLogLevel l : getLogLevels("null").getLogLevels()) {
            sb.append(l.getLoggerName()).append('=').append(l.getLoggerLevel()).append('\n');
        }
        return Response.ok(sb.toString(), MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Consumes("application/json")
    @Path("/setLevel")
    public Response setLogLevel(
        @QueryParam("logger") @DefaultValue("") String loggerName,
        @QueryParam("level") @DefaultValue("null") String loggerLevel) {

        changeLogLevel(loggerName, loggerLevel);
        return Response.ok().build();

    }

    private void changeLogLevel(String loggerName, String loggerLevel) {
        ch.qos.logback.classic.Logger loggerForName = (ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger(loggerName);

        if (loggerForName != null) {
            Level level = null;
            if (!loggerLevel.equals("null")) {
                level = Level.toLevel(loggerLevel);
            }
            loggerForName.setLevel(level);
            log.info("set logger=" + loggerForName.getName() + " to level=" + level);
        }
    }

    @POST
    @Consumes("application/json")
    @Path("/getLevels")
    @Produces("application/json")
    public JsonLogLevels getLogLevels(String tenantId) {
        log.info("listing logging levels");

        List<JsonLogLevel> logLevels = new LinkedList<JsonLogLevel>();

        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        List<Logger> loggerList = lc.getLoggerList();
        for (Logger logger : loggerList) {
            addToLogLevels(logger, logLevels);
        }

        Logger rl = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        addToLogLevels(rl, logLevels);
        return new JsonLogLevels(tenantId, logLevels);
    }

    private void addToLogLevels(Logger logger,
        List<JsonLogLevel> logLevels) {
        String level = (logger.getLevel() == null) ? null : logger.getLevel().toString();
        logLevels.add(new JsonLogLevel(logger.getName(), level));
    }

    @POST
    @Consumes("application/json")
    @Path("/setLevels")
    public void setLogLevels(JsonLogLevels jsonLogLevels) {

        for (JsonLogLevel l : jsonLogLevels.getLogLevels()) {
            changeLogLevel(l.getLoggerName(), l.getLoggerLevel());
        }
    }
}
