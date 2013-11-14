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
package com.jivesoftware.os.server.http.jetty.jersey.server.timedOperation;

import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import com.jivesoftware.os.jive.utils.logger.TimedOperation;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.server.internal.routing.UriRoutingContext;
import org.glassfish.jersey.server.model.ResourceMethodInvoker;

/**
 * Filter for Jersey service to add metrics logging to each endpoint.<br/>
 * Every call to service endpoint will create a {@link TimedOperation} instance to collect metric data for the endpoint call.<br/>
 * Use matched resource service class and method as the name of the operation (com.jivesoftware...serviceEndpoints>doSomething).<br/>
 * Use the http response code to identify the status of the operation (successful/failed).<br/>
 * Using {@link TimedOperationFilterClass} and {@link TimedOperationFilterMethod} you can control if metric logging will
 * run, the name of the timed operation and the http status to operation status mapping.<br/>
 * Also you can add more metrics to the timed operation of the request by accessing it via {@link #getOperation()}.<br/>
 * <p>
 * <b>Status code mapping</b><br/>
 * Default:<br/>
 * 0-399 = SUCCESFUL<br/>
 * 400-499 = FAILED_VALIDATION<br/>
 * 500- = FAILED<br/>
 * <br/>
 * You can overwrite status code mapping by setting {@link StatusRangeMap} in {@link TimedOperationFilterClass} and
 * {@link TimedOperationFilterMethod} where it will try to match {@link TimedOperationFilterMethod} ranges first, if
 * none matches it will try to match {@link TimedOperationFilterClass} ranges and if none matches either it will rever
 * to the default mapping.
 * </p>
 */
@Provider
public final class TimedOperationFilter implements ContainerRequestFilter, ContainerResponseFilter {

    //region: Fields and Consts

    /**
     * logger
     */
    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();

    /**
     * used to store the {@link TimedOperation} instance for each Jersey request on the request thread.
     */
    private static final ThreadLocal<Data> timedOperation = new ThreadLocal<>();


    //endregion

    /**
     * Init using IoC.
     */
    @Inject
    public TimedOperationFilter() {}

    /**
     * Get the timed operation for the current Jersey request.<br/>
     * Must be called on the request thread to get the timed operation, otherwise will return null.<br/>
     * Used to add additional data to the timed operation as needed.<br/>
     *
     * @return the timed operation instance for the request or null if not called on the request thread.
     */
    public static TimedOperation getOperation() {
        Data data = timedOperation.get();
        return data != null ? data.timedOperation : null;
    }

    /**
     * Start timed operation for the request.<br/>
     * Use matched resource service class and method as the name of the operation (com.jivesoftware...serviceEndpoints>doSomething).<br/>
     * Extract the tenant id from the request and set it in the operation.<br/>
     *
     * @param requestContext the http request data used for timed operation data.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        try {
            // get the matched resource service class, method and timed operations annotations
            UriRoutingContext routingContext = (UriRoutingContext) requestContext.getUriInfo();
            ResourceMethodInvoker invoker = (ResourceMethodInvoker) routingContext.getInflector();
            Class<?> resourceClass = invoker.getResourceClass();
            Method resourceMethod = invoker.getResourceMethod();
            TimedOperationFilterClass serviceEndpointsAnnot = resourceClass.getAnnotation(TimedOperationFilterClass.class);
            TimedOperationFilterMethod resourceAnnot = resourceMethod.getAnnotation(TimedOperationFilterMethod.class);

            // check if timed operation logging is enabled (can be explicitly enabled)
            boolean endpointsEnabled = serviceEndpointsAnnot == null || serviceEndpointsAnnot.enabled();
            boolean resourceEnabled = resourceAnnot == null || resourceAnnot.enabled();
            if ((endpointsEnabled && resourceEnabled) || (resourceAnnot != null && resourceEnabled)) {

                // get the name to be used for the operation
                String name = getOperationName(resourceClass, resourceMethod, serviceEndpointsAnnot, resourceAnnot);

                TimedOperation op = LOG.startTimedOperation(name);
                timedOperation.set(new Data(op, serviceEndpointsAnnot, resourceAnnot));
            }
        } catch (Throwable e) {
            // never throw exceptions
            LOG.warn("Exception in timed operation filter: " + e.getMessage(), e);
        }
    }

    /**
     * Stop the timed operation for the request.<br/>
     * Use the status code to set the status of the operation.<br/>
     * status < 400 - SUCCESS.<br/>
     * 400 <= status < 400 - FAILED_VALIDATION.<br/>
     * 500 < status - FAILED.<br/>
     *
     * @param responseContext the response to get more data on the operation.
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        try {
            Data data = timedOperation.get();
            if (data != null) {

                timedOperation.remove();
                int status = responseContext.getStatus();
                data.timedOperation.setStatus(getOperationStatus(status, data));
                data.timedOperation.stop();
            }
        } catch (Throwable e) {
            // never throw exceptions
            LOG.warn("Exception in timed operation filter: " + e.getMessage(), e);
        }
    }

    //region: Private methods

    /**
     * Get name either from the annotations or the method and service endpoints class name.
     */
    private String getOperationName(Class<?> resourceClass, Method resourceMethod, TimedOperationFilterClass serviceEndpointsAnnot,
        TimedOperationFilterMethod resourceAnnot) {
        String name = null;
        String className = null;
        String methodName = null;
        if (resourceAnnot != null && StringUtils.isNotEmpty(resourceAnnot.name())) {
            name = resourceAnnot.name();
        } else {
            if (serviceEndpointsAnnot != null && StringUtils.isNotEmpty(serviceEndpointsAnnot.className())) {
                className = serviceEndpointsAnnot.className();
            }
            if (resourceAnnot != null && StringUtils.isNotEmpty(resourceAnnot.methodName())) {
                methodName = resourceAnnot.methodName();
            }
        }

        // set the name to be used either if given or by class and method names
        if (name == null) {
            if (methodName == null) {
                methodName = resourceMethod.getName();
            }
            if (className == null) {
                className = resourceClass.getSimpleName();
            }
            name = MessageFormat.format("{0}>{1}", className, methodName);
        }
        return name;
    }

    /**
     * Get the operation status to set by the response status code.<br/>
     * First try to match ranges in method and class annotations in order and if none matches
     * use the default.
     */
    private static TimedOperation.Status getOperationStatus(int statusCode, Data data) {

        TimedOperation.Status status = null;

        if (data.timedOperationFilterMethod != null) {
            status = getOperationStatus(statusCode, data.timedOperationFilterMethod.statusRangeMaps());
        }

        if (status == null && data.timedOperationFilterClass != null) {
            status = getOperationStatus(statusCode, data.timedOperationFilterClass.statusRangeMaps());
        }

        // default mapping
        if (status == null) {
            if (statusCode < 400) {
                status = TimedOperation.Status.SUCCESSFUL;
            } else if (statusCode < 500) {
                status = TimedOperation.Status.FAILED_VALIDATION;
            } else {
                status = TimedOperation.Status.FAILED;
            }
        }
        return status;
    }

    /**
     * Get operation status by status code range mapping defined in annotations.
     */
    private static TimedOperation.Status getOperationStatus(int statusCode, StatusRangeMap[] maps) {
        if (maps != null && maps.length > 0) {
            for (StatusRangeMap map : maps) {
                if (map.from() <= statusCode && statusCode <= map.to()) {
                    return map.status();
                }
            }
        }
        return null;
    }
    //endregion

    //region: Inner class: Data

    /**
     * Used to hold the {@link TimedOperation}, {@link TimedOperationFilterClass} and {@link TimedOperationFilterMethod} for the request.
     */
    private final class Data {

        public final TimedOperation timedOperation;

        public final TimedOperationFilterClass timedOperationFilterClass;

        public final TimedOperationFilterMethod timedOperationFilterMethod;

        private Data(TimedOperation timedOperation, TimedOperationFilterClass timedOperationFilterClass,
            TimedOperationFilterMethod timedOperationFilterMethod) {
            this.timedOperation = timedOperation;
            this.timedOperationFilterClass = timedOperationFilterClass;
            this.timedOperationFilterMethod = timedOperationFilterMethod;
        }
    }
    //endregion
}
