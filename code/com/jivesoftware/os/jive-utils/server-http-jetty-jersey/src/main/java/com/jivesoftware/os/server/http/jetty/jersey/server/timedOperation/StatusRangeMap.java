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

import com.jivesoftware.os.jive.utils.logger.TimedOperation;
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
