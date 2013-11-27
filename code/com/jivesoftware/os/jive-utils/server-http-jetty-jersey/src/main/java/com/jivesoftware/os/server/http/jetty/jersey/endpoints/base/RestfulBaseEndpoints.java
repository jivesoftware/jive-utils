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
import com.jivesoftware.os.jive.utils.jaxrs.util.ResponseHelper;
import com.jivesoftware.os.jive.utils.logger.LoggerSummary;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import com.jivesoftware.os.server.http.health.check.FatalHealthCheck;
import com.jivesoftware.os.server.http.health.check.HealthCheckResponse;
import com.jivesoftware.os.server.http.health.check.HealthCheckService;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.annotation.Annotation;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
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
import org.apache.commons.lang.mutable.MutableLong;
import org.eclipse.jetty.server.Server;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

@Singleton
@Path("/")
public class RestfulBaseEndpoints {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();
    private static final Object LOCK = new Object();
    private static Set<Class<?>> reflected; // Synchronized on LOCK

    private final Server server;
    private final HealthCheckService healthCheckService;
    private final MutableLong lastGCTotalTime = new MutableLong();
    private final List<GarbageCollectorMXBean> garbageCollectors;

    public RestfulBaseEndpoints(@Context Server server, @Context HealthCheckService healthCheckService) {
        this.server = server;
        this.healthCheckService = healthCheckService;
        this.garbageCollectors = ManagementFactory.getGarbageCollectorMXBeans();
    }

    @GET
    @Path("/forceGC")
    public Response forceGC() {
        LOG.info("forced GC");
        Runtime.getRuntime().gc();
        return Response.ok().build();
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

    @GET
    @Path("/gcLoad")
    public Response gcLoad(@QueryParam("callback") @DefaultValue("") String callback) {

        GCLoad gcLoad = new GCLoad();
        try {

            long totalTimeInGC = 0;
            for (GarbageCollectorMXBean gc : garbageCollectors) {
                totalTimeInGC += gc.getCollectionTime();
            }
            long lastGC = lastGCTotalTime.longValue();
            gcLoad.percentageOfTimeCPUIsSpendingInGC = ((float) (totalTimeInGC - lastGC) / (float) lastGC);
            lastGCTotalTime.setValue(totalTimeInGC);

            LOG.info("gcLoad:" + gcLoad);
            if (callback.length() > 0) {
                return ResponseHelper.INSTANCE.jsonpResponse(callback, gcLoad);
            } else {
                return ResponseHelper.INSTANCE.jsonResponse(gcLoad);
            }

        } catch (Exception x) {
            LOG.warn("Failed to compute gc load.", x);
            return ResponseHelper.INSTANCE.errorResponse("Failed to compute gc load.", x);
        }

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
        return Response.ok().build();
    }

    @GET
    @Path("/interactionErrors")
    public Response executeInteractionErrors(@QueryParam("callback") @DefaultValue("") String callback) {
        LOG.info("Logged interaction errors:" + LoggerSummary.INSTANCE_EXTERNAL_INTERACTIONS.errors);
        if (callback.length() > 0) {
            return ResponseHelper.INSTANCE.jsonpResponse(callback, Long.toString(LoggerSummary.INSTANCE_EXTERNAL_INTERACTIONS.errors));
        }
        return Response.ok(Long.toString(LoggerSummary.INSTANCE_EXTERNAL_INTERACTIONS.errors), MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Path("/resetInteractionErrors")
    public Response resetInteractionErrors() {
        LOG.info("Logged interaction errors counter has been reset.");
        LoggerSummary.INSTANCE_EXTERNAL_INTERACTIONS.errors = 0;
        return Response.ok().build();
    }

    /** Easy to remember way to ensure a service is reachable. */
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

    /** Easy way to see the last ~10 errors, warns and infos */
    @GET
    @Path("/tail")
    public Response tail(@QueryParam("callback") @DefaultValue("") String callback) {
        File logFile = new File("./var/log/service.log");
        if (callback.length() > 0) {
            Tail tail = new Tail();
            tail.errors = Arrays.asList(LoggerSummary.INSTANCE.lastNErrors.get());
            tail.warns = Arrays.asList(LoggerSummary.INSTANCE.lastNWarns.get());
            tail.infos = Arrays.asList(LoggerSummary.INSTANCE.lastNInfos.get());
            tail.tail = tailLogFile(logFile, 80 * 500);
            return ResponseHelper.INSTANCE.jsonpResponse(callback, tail);
        } else {

            StringBuilder sb = new StringBuilder();
            sb.append(tailLogFile(logFile, 80 * 500));
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

    class Tail {
        public List<String> errors = new LinkedList<>();
        public List<String> warns = new LinkedList<>();
        public List<String> infos = new LinkedList<>();
        public String tail = "";
    }

    class Status {
        public boolean healthy = true;
        public List<HealthCheckResponse> healthCheckResponses = new ArrayList<>();
    }

    /** Summery of service */
    @GET
    @Path("/status")
    public Response status(@QueryParam("callback") @DefaultValue("") String callback) {
        Status status = new Status();
        status.healthCheckResponses = healthCheckService.checkHealth();
        for (HealthCheckResponse response : status.healthCheckResponses) {
            if (response instanceof FatalHealthCheck && !response.isHealthy()) {
                status.healthy = false;
                break;
            }
        }

        ResponseBuilder builder;
        if (status.healthy) {
            builder = Response.ok();
        } else {
            builder = Response.status(Response.Status.SERVICE_UNAVAILABLE);
        }
        if (callback.length() > 0) {
            return builder.entity(ResponseHelper.INSTANCE.jsonpResponse(callback, status).getEntity()).type(new MediaType("application", "javascript")).build();
        } else {
            return builder.entity(status).type(MediaType.APPLICATION_JSON).build();
        }

    }

    /** Summery of service */
    @GET
    @Path("/jettyStatus")
    public Response jettyStatus(@QueryParam("callback") @DefaultValue("") String callback) {
        if (callback.length() > 0) {
            JettyStatus status = new JettyStatus();
            status.state = server.getState();
            status.isLowOnThreads = server.getThreadPool().isLowOnThreads();
            status.serverNumThread = server.getThreadPool().getThreads();
            status.serverIdleThreads = server.getThreadPool().getIdleThreads();
            return ResponseHelper.INSTANCE.jsonpResponse(callback, status);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Server state=").append(server.getState()).append("\n");
            sb.append("Server thread pool isLowOnThreads=").append(server.getThreadPool().isLowOnThreads()).append("\n");
            sb.append("Server thread pool number=").append(server.getThreadPool().getThreads()).append("\n");
            sb.append("Server thread pool idle=").append(server.getThreadPool().getIdleThreads()).append("\n");

            @SuppressWarnings("rawtypes")
            Enumeration enumeration = server.getAttributeNames();
            if (enumeration != null) {
                while (enumeration.hasMoreElements()) {
                    Object next = enumeration.nextElement();
                    sb.append("Server attribute ").append(next).append("=").append(server.getAttribute(next.toString())).append(
                        '\n');
                }
            }

            return Response.ok(sb.toString(), MediaType.TEXT_PLAIN).build();
        }
    }

    class JettyStatus {
        public String state;
        public boolean isLowOnThreads;
        public int serverNumThread;
        public int serverIdleThreads;
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

    /** Summary of service */
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

    /** Summary of service */
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

    /** Summary of service */
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
