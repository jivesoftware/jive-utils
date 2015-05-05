package com.jivesoftware.os.jive.utils.http.client.rest;

/**
 *
 */
public class NonSuccessStatusCodeException extends RuntimeException {

    private final int statusCode;

    public NonSuccessStatusCodeException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
