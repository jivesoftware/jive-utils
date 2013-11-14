package com.jivesoftware.os.server.http.health.check;

public class HealthCheckResponseImpl implements HealthCheckResponse {

    private final String checkName;
    private final boolean isHealthy;
    private final String message;

    public HealthCheckResponseImpl(String checkName, boolean isHealthy, String message) {
        this.checkName = checkName;
        this.isHealthy = isHealthy;
        this.message = message;
    }

    @Override
    public String getCheckName() {
        return checkName;
    }

    @Override
    public boolean isHealthy() {
        return isHealthy;
    }

    @Override
    public String getStatusMessage() {
        return message;
    }

}
