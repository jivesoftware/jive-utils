// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth,
// inexhaustible as the great rivers.
// When they come to an end,
// they begin again,
// like the days and months;
// they die and are reborn,
// like the four seasons."
//
// - Sun Tsu,
// "The Art of War"

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
