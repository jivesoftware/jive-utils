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
