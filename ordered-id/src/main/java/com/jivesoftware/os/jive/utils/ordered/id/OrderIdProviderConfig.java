package com.jivesoftware.os.jive.utils.ordered.id;

import org.merlin.config.Config;
import org.merlin.config.defaults.IntDefault;

/**
 *
 *
 */
public interface OrderIdProviderConfig extends Config {
    @IntDefault(-1)
    int getWriterId();
}
