/*
 * $Revision: 142270 $
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */

package com.jivesoftware.os.server.http.jetty.jersey.endpoints.logging.metric;

import java.util.Map.Entry;

/**
 *
 * @author jonathan
 */
public class CounterStatusEntry implements Entry<String, Long> {
    private final String key;
    private final Long value;

    public CounterStatusEntry(String key, Long value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Long getValue() {
        return value;
    }

    @Override
    public Long setValue(Long value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
