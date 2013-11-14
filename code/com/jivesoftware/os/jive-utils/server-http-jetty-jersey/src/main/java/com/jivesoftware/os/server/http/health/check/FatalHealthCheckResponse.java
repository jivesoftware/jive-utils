package com.jivesoftware.os.server.http.health.check;

public class FatalHealthCheckResponse extends HealthCheckResponseImpl implements FatalHealthCheck {

    public FatalHealthCheckResponse(String checkName, boolean isHealthy, String message) {
        super(checkName, isHealthy, message);
    }
}
