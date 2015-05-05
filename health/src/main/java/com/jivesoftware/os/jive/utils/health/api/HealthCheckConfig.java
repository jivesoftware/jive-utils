package com.jivesoftware.os.jive.utils.health.api;

import org.merlin.config.Config;
import org.merlin.config.defaults.StringDefault;

/**
 * @author jonathan.colt
 */
public interface HealthCheckConfig extends Config {

    @StringDefault("unknownHealthCheckName")
    String getName();

    void setName(String name);

    @StringDefault("unknownDescription")
    String getDescription();

    void setDescription(String description);

}
