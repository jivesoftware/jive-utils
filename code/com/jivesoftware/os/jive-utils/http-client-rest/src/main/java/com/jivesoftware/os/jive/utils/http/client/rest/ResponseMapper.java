/*
 * Copyright 2015 Jive Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jivesoftware.os.jive.utils.http.client.rest;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 *
 * @author jonathan.colt
 */
public class ResponseMapper {

    private final ObjectMapper mapper;

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public ResponseMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public <T> T extractResultFromResponse(byte[] responseBody, Class<T> parametrized, Class<?>[] parameterClasses, T emptyResult) {
        JavaType resultType = mapper.getTypeFactory().constructParametricType(parametrized, parameterClasses);
        return extractResultFromResponse(responseBody, resultType, emptyResult);
    }

    public <T> T extractResultFromResponse(byte[] responseBody, Class<T> resultClass, T emptyResult) {
        if (responseBody == null) {
            return emptyResult;
        }

        T result;
        try {
            result = mapper.readValue(responseBody, 0, responseBody.length, resultClass);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing response body into result "
                + "object.  Response body was \"" + new String(responseBody, UTF_8)
                + "\".", e);
        }

        return result;
    }

    public <T> T extractResultFromResponse(byte[] responseBody, JavaType type, T emptyResult) {
        if (responseBody == null) {
            return emptyResult;
        }
        T result;
        try {
            result = mapper.readValue(responseBody, 0, responseBody.length, type);
        } catch (IOException e) {
            throw new RuntimeException("Error deserializing response body into result "
                + "object.  Response body was \"" + new String(responseBody, UTF_8)
                + "\".", e);
        }

        return result;
    }

    public boolean isSuccessStatusCode(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

}
