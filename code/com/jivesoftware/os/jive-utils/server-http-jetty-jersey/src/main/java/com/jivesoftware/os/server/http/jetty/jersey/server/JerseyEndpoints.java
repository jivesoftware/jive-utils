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
package com.jivesoftware.os.server.http.jetty.jersey.server;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Lists;
import com.jivesoftware.os.server.http.jetty.jersey.server.binding.Injectable;
import com.jivesoftware.os.server.http.jetty.jersey.server.binding.InjectableBinder;
import com.jivesoftware.os.server.http.jetty.jersey.server.filter.NewRelicRequestFilter;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.DispatcherType;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.HttpMethodOverrideFilter;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 *
 */
public class JerseyEndpoints implements HasServletContextHandler {

    private final Set<Class<?>> allClasses = new HashSet<>();
    private final Set<Class<?>> allInjectedClasses = new HashSet<>();
    private final Set<Object> allBinders = new HashSet<>();
    private final List<Injectable<?>> allInjectables = Lists.newArrayList();
    private boolean supportCORS = false;

    private final ObjectMapper mapper;

    public JerseyEndpoints() {
        this.mapper = new ObjectMapper()
            .configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, true);
    }

    public JerseyEndpoints addProvider(Class<?> provider) {
        allClasses.add(provider);
        return this;
    }

    public JerseyEndpoints addEndpoint(Class<?> jerseyEndpoint) {
        allClasses.add(jerseyEndpoint);
        return this;
    }

    public JerseyEndpoints addBinder(Binder requestInfoInjectable) {
        allBinders.add(requestInfoInjectable);
        return this;
    }

    public JerseyEndpoints addInjectable(Object injectableInstance) {
        return addInjectable(Injectable.of(injectableInstance));
    }

    public JerseyEndpoints addInjectable(Class<?> injectableClass, Object injectableInstance) {
        return addInjectable(Injectable.ofUnsafe(injectableClass, injectableInstance));
    }

    public JerseyEndpoints addInjectable(Injectable<?> injectable) {
        Class<?> injectableClass = injectable.getClazz();
        if (allInjectedClasses.contains(injectableClass)) {
            throw new IllegalStateException("You can only inject a single instance for any given class. You have already injected " + injectableClass);
        }
        allInjectedClasses.add(injectableClass);
        allInjectables.add(injectable);

        return this;
    }

    public JerseyEndpoints enableCORS() {
        supportCORS = true;
        return this;
    }

    public List<Injectable<?>> getInjectables() {
        return Collections.unmodifiableList(allInjectables);
    }

    public JerseyEndpoints humanReadableJson() {
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        return this;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    @Override
    public Handler getHandler(final Server server, String context, String applicationName) {
        ResourceConfig rc = new ResourceConfig()
            .registerClasses(allClasses)
            .register(HttpMethodOverrideFilter.class)
            .register(new JacksonFeature().withMapper(mapper))
            .register(MultiPartFeature.class) // adds support for multi-part API requests
            .registerInstances(allBinders)
            .registerInstances(
                new InjectableBinder(allInjectables),
                new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(server).to(Server.class);
                    }
                }
            );

        if (supportCORS) {
            rc.register(CorsContainerResponseFilter.class);
        }

        ServletHolder servletHolder = new ServletHolder(new ServletContainer(rc));
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        servletContextHandler.setContextPath(context);
        if (!applicationName.isEmpty()) {
            servletContextHandler.setDisplayName(applicationName);
        }
        servletContextHandler.addServlet(servletHolder, "/");
        servletContextHandler.addFilter(NewRelicRequestFilter.class, "/", EnumSet.of(DispatcherType.REQUEST));

        return servletContextHandler;
    }
}
