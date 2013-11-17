/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.server.http.jetty.jersey.server;

import com.jivesoftware.os.jive.utils.base.service.ServiceHandle;
import com.jivesoftware.os.server.http.health.check.HealthCheck;
import com.jivesoftware.os.server.http.health.check.HealthCheckService;
import com.jivesoftware.os.server.http.jetty.jersey.endpoints.base.RestfulBaseEndpoints;
import com.jivesoftware.os.server.http.jetty.jersey.endpoints.configuration.MainArgsConfigurationEndpoints;
import com.jivesoftware.os.server.http.jetty.jersey.endpoints.configuration.MainArgsConfigurationFile;
import com.jivesoftware.os.server.http.jetty.jersey.endpoints.killswitch.KillSwitch;
import com.jivesoftware.os.server.http.jetty.jersey.endpoints.killswitch.KillSwitchService;
import com.jivesoftware.os.server.http.jetty.jersey.endpoints.killswitch.KillSwitchsRestEndpoints;
import com.jivesoftware.os.server.http.jetty.jersey.endpoints.logging.level.LogLevelRestEndpoints;
import com.jivesoftware.os.server.http.jetty.jersey.endpoints.logging.metric.LogMetricRestfulEndpoints;
import java.util.Arrays;

public class RestfulManageServer implements ServiceHandle {

    private final RestfulServer server;
    private final HealthCheckService healthCheckService = new HealthCheckService();
    private final KillSwitchService killSwitchService = new KillSwitchService();
    private final JerseyEndpoints jerseyEndpoints;

    public RestfulManageServer(int port,
            String applicationName,
            String configFilePath,
            int maxNumberOfThreads,
            int maxQueuedRequests) {
        server = new RestfulServer(port, applicationName, maxNumberOfThreads, maxQueuedRequests);

        jerseyEndpoints = new JerseyEndpoints()
                .enableCORS()
                .humanReadableJson()
                .addEndpoint(RestfulBaseEndpoints.class).addInjectable(healthCheckService)
                .addEndpoint(MainArgsConfigurationEndpoints.class).addInjectable(new MainArgsConfigurationFile(configFilePath))
                .addEndpoint(LogMetricRestfulEndpoints.class)
                .addEndpoint(LogLevelRestEndpoints.class)
                .addEndpoint(KillSwitchsRestEndpoints.class).addInjectable(killSwitchService);

    }

    public void addEndpoint(Class clazz) {
        jerseyEndpoints.addEndpoint(clazz);
    }

    public void addInjectable(Class clazz, Object injectable) {
        jerseyEndpoints.addInjectable(clazz, injectable);
    }

    public RestfulManageServer initialize() {
        server.addContextHandler("/manage", jerseyEndpoints);
        return this;
    }

    public RestfulManageServer addHealthCheck(HealthCheck... check) {
        healthCheckService.addHealthCheck(Arrays.asList(check));
        return this;
    }

    public KillSwitch addKillSwitch(KillSwitch killSwitch) {
        return killSwitchService.add(killSwitch);
    }

    @Override
    public void start() throws Exception {
        server.start();
    }

    @Override
    public void stop() throws Exception {
        server.stop();
    }
}
