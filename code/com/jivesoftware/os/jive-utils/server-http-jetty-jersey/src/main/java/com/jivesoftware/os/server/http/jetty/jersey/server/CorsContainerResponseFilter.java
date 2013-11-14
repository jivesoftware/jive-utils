package com.jivesoftware.os.server.http.jetty.jersey.server;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import org.apache.commons.lang.StringUtils;

/**
 * Overly simplified support for CORS.  If we were using a version of jersey >= 2.0 we
 * could use ResourceModel overrides to detect what resources were available and dynamically
 * generate OPTIONS support for each endpoint.  This implementation blankets (too broadly)
 * support for requests to any existing endpoint and a fixed set of request methods.
 */
public class CorsContainerResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String requestMethod = requestContext.getMethod();
        String originHeader = requestContext.getHeaderString("Origin");
        boolean hasOriginHeader = StringUtils.isNotBlank(originHeader);
        boolean isOptionsRequest = "OPTIONS".equals(requestMethod);
        if (isOptionsRequest || hasOriginHeader) {
            String allowedOrigins = hasOriginHeader ? originHeader : "*";
            responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", allowedOrigins);
            responseContext.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
            responseContext.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            String reqHead = requestContext.getHeaderString("Access-Control-Request-Headers");
            if (StringUtils.isNotBlank(reqHead)) {
                responseContext.getHeaders().putSingle("Access-Control-Allow-Headers", reqHead);
            }
        }
    }
}
