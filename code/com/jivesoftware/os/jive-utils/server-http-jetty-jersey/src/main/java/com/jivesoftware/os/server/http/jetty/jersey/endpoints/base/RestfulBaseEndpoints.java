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
package com.jivesoftware.os.server.http.jetty.jersey.endpoints.base;

import com.google.common.base.Joiner;
import com.jivesoftware.os.jive.utils.base.interfaces.CallbackStream;
import com.jivesoftware.os.jive.utils.health.HealthCheckResponse;
import com.jivesoftware.os.jive.utils.health.HealthCheckService;
import com.jivesoftware.os.jive.utils.jaxrs.util.ResponseHelper;
import com.jivesoftware.os.mlogger.core.LoggerSummary;
import com.jivesoftware.os.mlogger.core.MetricLogger;
import com.jivesoftware.os.mlogger.core.MetricLoggerFactory;
import com.jivesoftware.os.server.http.jetty.jersey.endpoints.logging.metric.MetricsHelper;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.eclipse.jetty.server.Server;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.rendersnake.HtmlAttributes;
import org.rendersnake.HtmlAttributesFactory;
import org.rendersnake.HtmlCanvas;

@Singleton
@Path("/")
public class RestfulBaseEndpoints {

    static public class ResfulServiceName {

        public final String name;
        public final int port;

        public ResfulServiceName(String name, int port) {
            this.name = name;
            this.port = port;
        }

    }

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();
    private static final Object LOCK = new Object();
    private static Set<Class<?>> reflected; // Synchronized on LOCK

    private final ResfulServiceName resfulServiceName;
    private final Server server;
    private final HealthCheckService healthCheckService;
    private final File logFile;
    private final HasUI hasUI;

    public RestfulBaseEndpoints(@Context ResfulServiceName resfulServiceName,
        @Context Server server,
        @Context HealthCheckService healthCheckService,
        @Context File logFile,
        @Context HasUI hasUI) {

        this.resfulServiceName = resfulServiceName;
        this.server = server;
        this.healthCheckService = healthCheckService;
        this.logFile = logFile;
        this.hasUI = hasUI;
    }

    @GET
    @Path("/hasUI")
    public Response hasUI() {
        return ResponseHelper.INSTANCE.jsonResponse(hasUI);
    }

    @GET
    @Path("/ui")
    public Response ui(@Context UriInfo uriInfo) {
        LOG.info("ui");
        try {
            final HtmlCanvas canvas = new HtmlCanvas();
            canvas.html();
            canvas.body();

            canvas.h1().content("Service:" + resfulServiceName.name + ":" + resfulServiceName.port);
            List<HealthCheckResponse> checkHealth = healthCheckService.checkHealth();

            HtmlAttributes border = HtmlAttributesFactory.style("border: 1px solid  gray;");
            canvas.table(border);
            canvas.tr(HtmlAttributesFactory.style("background-color:#bbbbbb;"));
            canvas.td(border).content(String.valueOf("Health"));
            canvas.td(border).content(String.valueOf("Name"));
            canvas.td(border).content(String.valueOf("Status"));
            canvas.td(border).content(String.valueOf("Description"));
            canvas.td(border).content(String.valueOf("Resolution"));
            canvas.td(border).content(String.valueOf("Age in millis"));
            canvas._tr();
            for (HealthCheckResponse response : checkHealth) {
                if (-Double.MAX_VALUE != response.getHealth()) {
                    canvas.tr();
                    canvas.td(HtmlAttributesFactory.style("background-color:#" + getHEXTrafficlightColor(response.getHealth()) + ";"))
                        .content(String.valueOf(response.getHealth()));
                    canvas.td(border).content(String.valueOf(response.getName()));
                    canvas.td(border).content(String.valueOf(response.getStatus()));
                    canvas.td(border).content(String.valueOf(response.getDescription()));
                    canvas.td(border).content(String.valueOf(response.getResolution()));
                    canvas.td(border).content(String.valueOf(response.getTimestamp()));
                    canvas._tr();
                }
            }
            canvas._table();

            canvas.hr();
            canvas.h2().content("Recent Internal Errors: " + LoggerSummary.INSTANCE.errors);
            canvas.pre().content(Joiner.on("\n").join(orEmpty(LoggerSummary.INSTANCE.lastNErrors.get())));

            canvas.h2().content("Recent Interaction Errors: " + LoggerSummary.INSTANCE_EXTERNAL_INTERACTIONS.errors);
            canvas.pre().content(Joiner.on("\n").join(orEmpty(LoggerSummary.INSTANCE_EXTERNAL_INTERACTIONS.lastNErrors.get())));

            canvas.h2().content("Recent Infos");
            canvas.pre().content(Joiner.on("\n").join(orEmpty(LoggerSummary.INSTANCE.lastNInfos.get())));

            canvas.form(HtmlAttributesFactory.action(uriInfo.getBaseUri().getPath() + "resetErrors").method("get").id("errors-form"))
                .fieldset()
                .input(HtmlAttributesFactory.type("submit").value("Reset Errors"))
                ._fieldset()
                ._form();

            canvas.form(HtmlAttributesFactory.action(uriInfo.getBaseUri().getPath() + "tail").method("get").id("tail-form"))
                .fieldset()
                .input(HtmlAttributesFactory.type("submit").value("Tail"))
                ._fieldset()
                ._form();

            canvas.hr();
            canvas.h1().content("Counters: ");
            canvas.table();
            canvas.tr();
            canvas.td().content("Count");
            canvas.td().content("Name");
            canvas._tr();

            MetricsHelper.INSTANCE.getCounters("").getAll(new CallbackStream<Map.Entry<String, Long>>() {

                @Override
                public Map.Entry<String, Long> callback(Map.Entry<String, Long> v) throws Exception {
                    if (v != null) {
                        canvas.tr();
                        canvas.td().content(String.valueOf(v.getValue()));
                        canvas.td().content(v.getKey());
                        canvas._tr();
                    }
                    return v;
                }
            });

            canvas._table();

            canvas.hr();
            canvas.h1().content("Timers: ");
            canvas.table();
            canvas.tr();
            canvas.td().content("Timer");
            canvas.td().content("Name");
            canvas._tr();

            MetricsHelper.INSTANCE.getTimers("").getAll(new CallbackStream<Map.Entry<String, Long>>() {

                @Override
                public Map.Entry<String, Long> callback(Map.Entry<String, Long> v) throws Exception {
                    if (v != null) {
                        canvas.tr();
                        canvas.td().content(String.valueOf(v.getValue()));
                        canvas.td().content(v.getKey());
                        canvas._tr();
                    }
                    return v;
                }
            });

            canvas._table();

            canvas.hr();

            canvas.form(HtmlAttributesFactory.action(uriInfo.getBaseUri().getPath() + "logging/setLogLevel").method("get").id("setLogLevel-form"));
            canvas.fieldset();

            org.apache.logging.log4j.Logger rl = LogManager.getRootLogger();
            if (rl instanceof Logger) {
                LoggerContext rlContext = ((Logger) rl).getContext();
                Collection<Logger> loggers = rlContext.getLoggers();

                canvas.select(HtmlAttributesFactory.name("logger"));
                for (Logger logger : loggers) {
                    try {
                        Class.forName(logger.getName());
                        String level = (logger.getLevel() == null) ? null : logger.getLevel().toString();
                        canvas.option(HtmlAttributesFactory.value(logger.getName())).content(level + "=" + logger.getName());
                    } catch (ClassNotFoundException e) {
                    }

                }
                canvas._select();
            } else {
                canvas.h1().content("Loggers unavailable, RootLogger is: " + rl.getClass().getName() + " expected: " + Logger.class.getName());
            }

            canvas.select(HtmlAttributesFactory.name("level"));
            canvas.option(HtmlAttributesFactory.value("")).content("Inherit");
            canvas.option(HtmlAttributesFactory.value("TRACE")).content("TRACE");
            canvas.option(HtmlAttributesFactory.value("DEBUG")).content("DEBUG");
            canvas.option(HtmlAttributesFactory.value("INFO")).content("INFO");
            canvas.option(HtmlAttributesFactory.value("WARN")).content("WARN");
            canvas.option(HtmlAttributesFactory.value("ERROR")).content("ERROR");
            canvas.option(HtmlAttributesFactory.value("OFF")).content("OFF");
            canvas._select();

            canvas.input(HtmlAttributesFactory.type("submit").value("Change"))
                ._fieldset()
                ._form();

            canvas.hr();

            canvas.form(HtmlAttributesFactory.action(uriInfo.getBaseUri().getPath() + "system/env").method("get").id("sysEnv-form"))
                .fieldset()
                .input(HtmlAttributesFactory.type("submit").value("Env Properties"))
                ._fieldset()
                ._form();

            canvas.form(HtmlAttributesFactory.action(uriInfo.getBaseUri().getPath() + "system/properties").method("get").id("sysProps-form"))
                .fieldset()
                .input(HtmlAttributesFactory.type("submit").value("System Properties"))
                ._fieldset()
                ._form();

            canvas.form(HtmlAttributesFactory.action(uriInfo.getBaseUri().getPath() + "threadDump").method("get").id("threadDump-form"))
                .fieldset()
                .input(HtmlAttributesFactory.type("submit").value("Thread Dump"))
                ._fieldset()
                ._form();

            canvas.form(HtmlAttributesFactory.action(uriInfo.getBaseUri().getPath() + "forceGC").method("get").id("forceGC-form"))
                .fieldset()
                .input(HtmlAttributesFactory.type("submit").value("ForceGC"))
                ._fieldset()
                ._form();

            canvas.form(HtmlAttributesFactory.action(uriInfo.getBaseUri().getPath() + "shutdown").method("get").id("shutdown-form"))
                .fieldset()
                .input(HtmlAttributesFactory.type("text").name("userName").value("anonymous"))
                .input(HtmlAttributesFactory.type("submit").value("Shutdown"))
                ._fieldset()
                ._form();

            canvas._body();
            canvas._html();
            return Response.ok(canvas.toHtml(), MediaType.TEXT_HTML).build();
        } catch (Exception x) {
            LOG.warn("Failed build UI html.", x);
            return ResponseHelper.INSTANCE.errorResponse("Failed build UI html.", x);
        }
    }

    String getHEXTrafficlightColor(double value) {
        String s = Integer.toHexString(Color.HSBtoRGB((float) value / 3f, 1f, 1f) & 0xffffff);
        return "000000".substring(s.length()) + s;
    }

    private String[] orEmpty(String[] strings) {
        return (strings == null) ? new String[]{""} : emptyNulls(strings);
    }

    private String[] emptyNulls(String[] strings) {
        for (int i = 0; i < strings.length; i++) {
            if (strings[i] == null) {
                strings[i] = "";
            }
        }
        return strings;
    }

    @GET
    @Path("/forceGC")
    public Response forceGC() {
        LOG.info("forced GC");
        Runtime.getRuntime().gc();
        return Response.ok("Forced GC", MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Path("/threadDump")
    @Produces(MediaType.TEXT_PLAIN)
    public Response stackDump() {
        StringBuilder builder = new StringBuilder();
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            builder.append(String.format("\"%s\" %s prio=%d tid=%d nid=1 %s\njava.lang.Thread.State: %s\n",
                thread.getName(),
                (thread.isDaemon() ? "daemon" : ""),
                thread.getPriority(),
                thread.getId(),
                Thread.State.WAITING.equals(thread.getState()) ? "in Object.wait()" : thread.getState().name().toLowerCase(),
                (thread.getState().equals(Thread.State.WAITING) ? "WAITING (on object monitor)" : thread.getState())));
            final StackTraceElement[] stackTraceElements = thread.getStackTrace();
            for (final StackTraceElement stackTraceElement : stackTraceElements) {
                builder.append("\n        at ");
                builder.append(stackTraceElement);
            }
            builder.append("\n\n");
        }
        return Response.ok(builder.toString()).build();
    }

    class GCLoad {

        public float percentageOfTimeCPUIsSpendingInGC = 0f;

        @Override
        public String toString() {
            return "GCLoad{" + "percentageOfTimeCPUIsSpendingInGC=" + percentageOfTimeCPUIsSpendingInGC + '}';
        }
    }

    @GET
    @Path("/errors")
    public Response executeErrors(@QueryParam("callback") @DefaultValue("") String callback) {
        LOG.info("Logged errors:" + LoggerSummary.INSTANCE.errors);
        if (callback.length() > 0) {
            return ResponseHelper.INSTANCE.jsonpResponse(callback, Long.toString(LoggerSummary.INSTANCE.errors));
        }
        return Response.ok(Long.toString(LoggerSummary.INSTANCE.errors), MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Path("/resetErrors")
    public Response resetErrors() {
        LOG.info("Logged errors counter has been reset.");
        LoggerSummary.INSTANCE.errors = 0;
        LoggerSummary.INSTANCE.lastNErrors.clear("");
        LoggerSummary.INSTANCE_EXTERNAL_INTERACTIONS.errors = 0;
        LoggerSummary.INSTANCE_EXTERNAL_INTERACTIONS.lastNErrors.clear("");
        return Response.ok("Reset Errors", MediaType.TEXT_PLAIN).build();
    }

    /**
     * Easy to remember way to ensure a service is reachable.
     *
     * @param callback
     * @return
     */
    @GET
    @Path("/ping")
    public Response executePing(@QueryParam("callback") @DefaultValue("") String callback) {
        if (callback.length() > 0) {
            return ResponseHelper.INSTANCE.jsonpResponse(callback, new Ping());
        }
        return Response.ok("ping", MediaType.TEXT_PLAIN).build();
    }

    class Ping {

        public String ping = "ping";
    }

    @GET
    @Path("/system/env")
    public Response env(@QueryParam("callback") @DefaultValue("") String callback) {
        if (callback.length() > 0) {
            Env env = new Env();
            for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
                env.systemEnv.put(entry.getKey(), entry.getValue());
            }
            return ResponseHelper.INSTANCE.jsonpResponse(callback, env);
        } else {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
                sb.append("env:").append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }
            return Response.ok(sb.toString(), MediaType.TEXT_PLAIN).build();
        }

    }

    class Env {

        public Map<String, String> systemEnv = new HashMap<>();
    }

    @GET
    @Path("/system/properties")
    public Response properties(@QueryParam("callback") @DefaultValue("") String callback) {
        if (callback.length() > 0) {
            Env env = new Env();
            for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
                env.systemEnv.put(entry.getKey().toString(), entry.getValue().toString());
            }
            return ResponseHelper.INSTANCE.jsonpResponse(callback, env);
        } else {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
                sb.append("property:").append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }
            return Response.ok(sb.toString(), MediaType.TEXT_PLAIN).build();
        }

    }

    class SystemProperties {

        public Map<String, String> systemProperties = new HashMap<>();
    }

    /**
     * Easy way to see the last ~10 errors, warns and infos
     *
     * @param nLines
     * @param callback
     * @return
     */
    @GET
    @Path("/tail")
    public Response tail(@QueryParam("lastNLines") @DefaultValue("100") int nLines,
        @QueryParam("callback") @DefaultValue("") String callback) {
        if (callback.length() > 0) {
            return ResponseHelper.INSTANCE.jsonpResponse(callback, tailLogFile(logFile, 80 * nLines));
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(tailLogFile(logFile, 80 * nLines));
            return Response.ok(sb.toString(), MediaType.TEXT_PLAIN).build();
        }
    }

    private String tailLogFile(File file, int lastNBytes) {
        if (!file.exists()) {
            return "Log file:" + file.getAbsolutePath() + " doesnt exist?";
        }
        try (RandomAccessFile fileHandler = new RandomAccessFile(file, "r")) {
            long fileLength = file.length() - 1;
            long start = fileLength - lastNBytes;
            if (start < 0) {
                start = 0;
            }
            byte[] bytes = new byte[(int) (fileLength - start)];
            fileHandler.seek(start);
            fileHandler.readFully(bytes);
            return new String(bytes, "ASCII");
        } catch (FileNotFoundException e) {
            LOG.warn("Tailing failed locate file. " + file);
            return "Tailing failed locate file. " + file;
        } catch (IOException e) {
            LOG.warn("Tailing file encountered the following error. " + file, e);
            return "Tailing file encountered the following error. " + file;
        }
    }

    @GET
    @Path("/recentErrors")
    public Response recentErrors(@QueryParam("callback") @DefaultValue("") String callback) {
        if (callback.length() > 0) {
            return ResponseHelper.INSTANCE.jsonpResponse(callback, LoggerSummary.INSTANCE.lastNErrors.get());
        } else {
            return ResponseHelper.INSTANCE.jsonResponse(LoggerSummary.INSTANCE.lastNErrors.get());
        }

    }

    class Health {

        public double health = 1.0d;
        public List<HealthCheckResponse> healthChecks = new ArrayList<>();
    }

    /**
     * Health of service
     *
     * @param callback
     * @return
     */
    @GET
    @Path("/health")
    public Response health(@QueryParam("callback") @DefaultValue("") String callback) {
        try {
            Health health = new Health();
            health.healthChecks = healthCheckService.checkHealth();
            for (HealthCheckResponse response : health.healthChecks) {
                if (-Double.MAX_VALUE != response.getHealth()) {
                    health.health = Math.min(health.health, response.getHealth());
                }
            }

            ResponseBuilder builder;
            if (health.health > 0.0d) {
                builder = Response.ok();
            } else {
                builder = Response.status(Response.Status.SERVICE_UNAVAILABLE);
            }
            if (callback.length() > 0) {
                return builder.entity(ResponseHelper.INSTANCE.jsonpResponse(callback, health).getEntity()).type(new MediaType("application", "javascript")).
                    build();
            } else {
                return builder.entity(health).type(MediaType.APPLICATION_JSON).build();
            }
        } catch (Exception x) {
            LOG.warn("Failed to get health.", x);
            return ResponseHelper.INSTANCE.errorResponse("Failed to get health.", x);
        }

    }

    class JettyStatus {

        public String state;
        public boolean isLowOnThreads;
        public int serverNumThread;
        public int serverIdleThreads;
    }

    /**
     * JettyStatus for service
     *
     * @param callback
     * @return
     */
    @GET
    @Path("/jettyStatus")
    public Response jettyStatus(@QueryParam("callback")
        @DefaultValue("") String callback
    ) {

        JettyStatus status = new JettyStatus();
        status.state = server.getState();
        status.isLowOnThreads = server.getThreadPool().isLowOnThreads();
        status.serverNumThread = server.getThreadPool().getThreads();
        status.serverIdleThreads = server.getThreadPool().getIdleThreads();
        if (callback.length() > 0) {
            return ResponseHelper.INSTANCE.jsonpResponse(callback, status);
        } else {
            return ResponseHelper.INSTANCE.jsonResponse(status);
        }
    }

    @GET
    @Path("/help")
    public Response help() {
        try {
            Set<Class<?>> annotated = getReflected();
            StringBuilder manage_sb = new StringBuilder();
            StringBuilder sb = new StringBuilder();
            StringBuilder test_sb = new StringBuilder();

            appendEndpoints(annotated, manage_sb, sb, test_sb);
            sb.append(manage_sb);
            sb.append(test_sb);
            return Response.ok(sb.toString(), MediaType.TEXT_PLAIN).build();
        } catch (Exception x) {
            LOG.error("Failed to build help response.", x);
            return ResponseHelper.INSTANCE.errorResponse("failed to build help message.", x);
        }
    }

    private static Set<Class<?>> getReflected() {
        synchronized (LOCK) {
            if (reflected != null) {
                return reflected;
            }
            Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("com.jivesoftware"))
                .setScanners(new MethodAnnotationsScanner(), new TypeAnnotationsScanner()));
            Set<Class<?>> result = reflections.getTypesAnnotatedWith(Path.class);
            Set<Method> methods = reflections.getMethodsAnnotatedWith(Path.class);
            for (Method method : methods) {
                result.add(method.getDeclaringClass());
            }
            reflected = Collections.unmodifiableSet(result);
            return reflected;
        }
    }

    private void appendEndpoints(Set<Class<?>> annotated, StringBuilder manage_sb, StringBuilder sb, StringBuilder test_sb) {
        sb.append("------------------------------\n");
        sb.append("  Service Endpoints \n");
        sb.append("------------------------------\n");
        manage_sb.append("------------------------------\n");
        manage_sb.append("  Management Endpoints\n");
        manage_sb.append("------------------------------\n");

        for (Class<?> a : annotated) {
            StringBuilder output = new StringBuilder();

            // Get the context for the endpoint. If it is shipped as a default endpoint
            // prepend the /manage context onto it.
            String context = a.getAnnotation(Path.class).value();
            if ("/".equals(context)) {
                context = "";
            }
            if (a.getName().contains("com.jivesoftware.jive.deployer")) {
                if (!a.getName().contains("endpoints.testable")) {
                    context = "/manage" + context;
                } else {
                    context = "/test" + context;
                }
            }

            for (Method m : a.getMethods()) {
                Annotation path = m.getAnnotation(Path.class);
                if (path == null) {
                    continue;
                }
                String verb = verb(m);
                if (verb == null) {
                    continue;
                }
                StringBuilder signature = new StringBuilder(context);
                String value = m.getAnnotation(Path.class).value();
                if ("/".equals(value)) {
                    value = "";
                }
                signature.append(value);

                Class<?>[] ptypes = m.getParameterTypes();

                boolean first = true;
                Annotation[][] annotationses = m.getParameterAnnotations();
                for (int i = 0; i < ptypes.length; i++) {
                    for (int j = 0; j < annotationses[i].length; j++) {
                        if (annotationses[i][j] instanceof QueryParam) {
                            if (first) {
                                signature.append("?");
                                first = false;
                            } else {
                                signature.append("&");
                            }
                            signature.append(((QueryParam) annotationses[i][j]).value());
                            signature.append("=");
                        } else if (annotationses[i][j] instanceof DefaultValue) {
                            signature.append(((DefaultValue) annotationses[i][j]).value());
                        } else if (annotationses[i][j] instanceof PathParam) {
                            // noop to surpress useless output
                        } else if (annotationses[i][j] instanceof HeaderParam) {
                            signature.append("[Header: " + ((HeaderParam) annotationses[i][j]).value() + "]");
                        } else if (annotationses[i][j] instanceof FormParam) {
                            signature.append("[FormParam: " + ((FormParam) annotationses[i][j]).value() + "]");
                        } else if (annotationses[i][j] instanceof Context) {
                            // noop to surpress useless output
                        } else {
                            signature.append(annotationses[i][j].toString());
                        }
                        if (signature.indexOf("callback=") > 0 && signature.indexOf("jsonp") <= 0) {
                            signature.delete(signature.length() - 10, signature.length());

                            StringBuilder padding = new StringBuilder();
                            long requiredSpaces = 70 - signature.length();
                            for (int k = 0; k <= requiredSpaces; k++) {
                                padding.append(" ");
                            }
                            signature.append(padding);
                            signature.append("(supports jsonp via callback=)");
                        }
                    }
                }
                output.append(verb).append(signature).append("\n");
            }
            if (a.getName().contains("com.jivesoftware.jive.deployer")) {
                if (!a.getName().contains("endpoints.testable")) {
                    manage_sb.append(output);
                }
            } else {
                sb.append(output);
            }
        }

    }

    /**
     * Summary of service
     *
     * @param callback
     * @return
     */
    @GET
    @Path("/classpath")
    public Response classpath(@QueryParam("callback") @DefaultValue("") String callback) {

        String classpath = System.getProperty("java.class.path");
        if (callback.length() > 0) {
            return ResponseHelper.INSTANCE.jsonpResponse(callback, classpath);
        } else {
            return ResponseHelper.INSTANCE.jsonResponse(classpath);
        }
    }

    /**
     * Summary of service
     *
     * @param callback
     * @return
     */
    @GET
    @Path("/releaseNotes")
    public Response releaseNotes(@QueryParam("callback") @DefaultValue("") String callback) {

        String classpath = System.getProperty("java.class.path");

        List<String> allReleaseNotes = new LinkedList<>();
        for (String source : classpath.split(":")) {
            if (source.endsWith(".jar")) {
                LocateStringResource jar = new LocateStringResource(source, "release-notes.json");

                String releaseNotes = jar.getStringResource();
                if (releaseNotes == null) {
                    continue;
                }
                allReleaseNotes.add(releaseNotes);
            }
        }
        String jsonString = "[" + Joiner.on("\n").join(allReleaseNotes) + "]";
        if (callback.length() > 0) {
            jsonString = callback + "(" + jsonString + ");";
            return Response.ok().entity(jsonString).type(MediaType.APPLICATION_JSON_TYPE).build();
        } else {
            return Response.ok().entity(jsonString).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    /**
     * Summary of service
     *
     * @param callback
     * @return
     */
    @GET
    @Path("/releaseNote")
    public Response releaseNote(@QueryParam("callback") @DefaultValue("") String callback) {

        String classpath = System.getProperty("java.class.path");

        List<String> allReleaseNotes = new LinkedList<>();
        for (String source : classpath.split(":")) {
            String[] segments = source.split("/");
            String jarFile = segments[segments.length - 1];
            if (jarFile.endsWith(".jar") && jarFile.contains("deployable")) {
                LocateStringResource jar = new LocateStringResource(source, "release-notes.json");

                String releaseNotes = jar.getStringResource();
                if (releaseNotes == null) {
                    continue;
                }
                allReleaseNotes.add(releaseNotes);
            }
        }
        String jsonString = "[" + Joiner.on("\n").join(allReleaseNotes) + "]";
        if (callback.length() > 0) {
            jsonString = callback + "(" + jsonString + ");";
            return Response.ok().entity(jsonString).type(MediaType.APPLICATION_JSON_TYPE).build();
        } else {
            return Response.ok().entity(jsonString).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
    }

    /**
     * Summary of service
     *
     * @param callback
     * @return
     */
    @GET
    @Path("/version")
    public Response version(@QueryParam("callback") @DefaultValue("") String callback) {

        String classpath = System.getProperty("java.class.path");

        for (String source : classpath.split(":")) {
            if (source.endsWith(".jar") && source.contains("-deployable")) {
                LocateStringResource jar = new LocateStringResource(source, "release-notes.json");

                String releaseNotes = jar.getStringResource();
                if (releaseNotes == null) {
                    continue;
                }
                if (callback.length() > 0) {
                    releaseNotes = callback + "(" + releaseNotes + ");";
                    return Response.ok().entity(releaseNotes).type(MediaType.APPLICATION_JSON_TYPE).build();
                } else {
                    return Response.ok().entity(releaseNotes).type(MediaType.APPLICATION_JSON_TYPE).build();
                }
            }
        }

        return ResponseHelper.INSTANCE.errorResponse("couldn't find release-notes.json.");
    }

    @GET
    @Path("/shutdown")
    public Response shutdown(@QueryParam("userName") @DefaultValue("anonymous") final String userName) {
        if (userName.equals("anonymous")) {
            LOG.info("user anonymous is trying to shutdown this service. This currently isn't supported! Please provide a valid userName.");
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        LOG.info("service is being manually shutdown via rest endpoint by userName:" + userName);
        System.exit(0);
        return Response.ok().build();

    }

    private String verb(Method m) {
        if (m.getAnnotation(DELETE.class) != null) {
            return "DELETE  ";
        } else if (m.getAnnotation(GET.class) != null) {
            return "GET     ";
        } else if (m.getAnnotation(HEAD.class) != null) {
            return "HEAD    ";
        } else if (m.getAnnotation(OPTIONS.class) != null) {
            return "OPTIONS ";
        } else if (m.getAnnotation(POST.class) != null) {
            return "POST    ";
        } else if (m.getAnnotation(PUT.class) != null) {
            return "PUT     ";
        } else {
            return null;
        }
    }

}
