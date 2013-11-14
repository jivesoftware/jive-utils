package com.jivesoftware.os.jive.utils.jaxrs.util;

import com.fasterxml.jackson.databind.JsonNode;

/** Struct used for capturing error information in a JSON response. */
public class ErrorResponse {

    public ErrorResponse(String message, String trace, JsonNode relatedData) {
        this.message = message;
        this.trace = trace;
        this.relatedData = relatedData;
    }

    /** Error message */
    public final String message;
    /** Any related stacktrace */
    public final String trace;
    /** Data that caused or is related to the error */
    public final JsonNode relatedData;
}
