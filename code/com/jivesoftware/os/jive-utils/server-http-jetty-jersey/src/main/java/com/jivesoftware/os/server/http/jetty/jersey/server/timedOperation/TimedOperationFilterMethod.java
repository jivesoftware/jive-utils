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
package com.jivesoftware.os.server.http.jetty.jersey.server.timedOperation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used by {@link TimedOperationFilter} to control the metric data logged using timed operation.<br/>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TimedOperationFilterMethod {

    /**
     * Set to disable timed operation logging for the method (default - true)
     */
    boolean enabled() default true;

    /**
     * Set to overwrite the name of the timed operation.<br/>
     * Note: this will overwrite the full name that includes the class name and
     * the method name pair.
     */
    String name() default "";

    /**
     * Set to overwrite the method name of the timed operation.<br/>
     * The name will be combined between the class name and this method name.
     */
    String methodName() default "";

    /**
     * Set custom mapping of http response status code to timed operation status.<br/>
     * Can have multiple ranges for fine granularity.<br/>
     * If http status code does not match given ranges will use the default mapping.
     */
    StatusRangeMap[] statusRangeMaps() default { };
}
