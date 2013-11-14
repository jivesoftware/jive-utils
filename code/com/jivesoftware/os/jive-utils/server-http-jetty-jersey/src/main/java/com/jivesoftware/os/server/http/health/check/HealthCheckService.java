package com.jivesoftware.os.server.http.health.check;

import java.util.ArrayList;
import java.util.List;

public class HealthCheckService {

    private final List<HealthCheck> healthChecks = new ArrayList<>();

    public void addHealthCheck(List<HealthCheck> toAdd) {
        healthChecks.addAll(toAdd);
    }

    public List<HealthCheckResponse> checkHealth() {
        List<HealthCheckResponse> response = new ArrayList<>();
        for (HealthCheck healthCheck : healthChecks) {
            response.add(healthCheck.checkHealth());
        }
        return response;
    }

}
