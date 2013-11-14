package com.jivesoftware.os.server.http.health.check;

public interface HealthCheckResponse {

    String getCheckName();

    boolean isHealthy();

    String getStatusMessage();

}
