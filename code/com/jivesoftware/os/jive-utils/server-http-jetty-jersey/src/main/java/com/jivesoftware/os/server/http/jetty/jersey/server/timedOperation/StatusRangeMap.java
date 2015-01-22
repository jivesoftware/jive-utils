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

import com.jivesoftware.os.mlogger.core.TimedOperation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Set custom mapping of http response status code to timed operation status.<br/>
 * The 'from' and 'to' parameters of the range are inclusive (from <= code <= to).<br/>
 * To capture single value code set the 'from' and 'to' fields to the same value.<br/>
 * <p>
 * <b>Example:</b><br/>
 * from=200<br/>
 * to=299<br/>
 * status = {@link TimedOperation.Status#SUCCESSFUL}<br/>
 * </p>
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface StatusRangeMap {

    /**
     * The low bound of the range to map status codes
     */
    int from();

    /**
     * The high bound of the range to map status code
     */
    int to();

    /**
     * The timed operation status to map the http codes to
     */
    TimedOperation.Status status();
}
