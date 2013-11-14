package com.jivesoftware.os.server.http.jetty.jersey.endpoints.logging.level;

import com.google.inject.Singleton;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import java.util.Enumeration;
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
import org.apache.log4j.Logger;

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
        org.apache.log4j.Logger loggerForName;
        if (loggerName.equals(org.apache.log4j.LogManager.getRootLogger().getName())) {
            loggerForName = org.apache.log4j.LogManager.getRootLogger();
        } else {
            loggerForName = org.apache.log4j.LogManager.getLogger(loggerName);
        }

        if (loggerForName != null) {
            org.apache.log4j.Level level = null;
            if (!loggerLevel.equals("null")) {
                level = org.apache.log4j.Level.toLevel(loggerLevel);
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

        @SuppressWarnings("rawtypes")
        Enumeration enumeration = org.apache.log4j.LogManager.getCurrentLoggers();
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                Object next = enumeration.nextElement();
                if (next instanceof org.apache.log4j.Logger) {
                    addToLogLevels((Logger) next, logLevels);
                } else {
                    log.warn("unexpected logger class " + next);
                }
            }
        }

        org.apache.log4j.Logger rl = org.apache.log4j.LogManager.getRootLogger();
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
