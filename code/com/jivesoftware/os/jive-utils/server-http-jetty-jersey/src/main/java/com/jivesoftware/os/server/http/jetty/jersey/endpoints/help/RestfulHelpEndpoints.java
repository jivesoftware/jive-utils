package com.jivesoftware.os.server.http.jetty.jersey.endpoints.help;

import com.jivesoftware.os.jive.utils.jaxrs.util.ResponseHelper;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

@Singleton
@Path("/")
public class RestfulHelpEndpoints {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();
    private static final Object LOCK = new Object();
    private static Set<Class<?>> reflected; // Synchronized on LOCK

    public RestfulHelpEndpoints() {
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

        StringBuilder testableEndpointList = new StringBuilder();
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
                            signature.append("[Header: ").append(((HeaderParam) annotationses[i][j]).value()).append("]");
                        } else if (annotationses[i][j] instanceof FormParam) {
                            signature.append("[FormParam: ").append(((FormParam) annotationses[i][j]).value()).append("]");
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
                } else {
                    testableEndpointList.append(output);
                }
            } else {
                sb.append(output);
            }
        }

        test_sb.append(testableEndpointList);
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
