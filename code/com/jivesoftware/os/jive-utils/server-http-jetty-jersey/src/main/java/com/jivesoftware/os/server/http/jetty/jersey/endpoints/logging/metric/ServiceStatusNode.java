/*
 * $Revision: 142270 $
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */

package com.jivesoftware.os.server.http.jetty.jersey.endpoints.logging.metric;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jivesoftware.os.jive.utils.base.interfaces.CallbackStream;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author jonathan
 */
public class ServiceStatusNode {
    public final ConcurrentHashMap<String, ServiceStatusNode> nodes = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, Long> leafs = new ConcurrentHashMap<>();

    @JsonIgnore
    public Long getLeaf(String key) {
        return leafs.get(key);
    }

    @JsonIgnore
    public void putLeafs(String key, Long leaf) {
        leafs.put(key, leaf);
    }

    @JsonIgnore
    public ServiceStatusNode getNode(String key) {
        return nodes.get(key);
    }

    @JsonIgnore
    ServiceStatusNode putNode(String key) {
        ServiceStatusNode got = nodes.get(key);
        if (got != null) {
            return got;
        }
        got = new ServiceStatusNode();
        ServiceStatusNode had = nodes.putIfAbsent(key, got);
        if (had != null) {
            got = had;
        }
        return got;
    }

    @JsonIgnore
    void callback(String key,
        CallbackStream<Entry<String, Long>> stream) throws Exception {
        for (Entry<String, Long> e : leafs.entrySet()) {
            stream.callback(new CounterStatusEntry(key + ">" + e.getKey(), e.getValue()));
        }
        for (Entry<String, ServiceStatusNode> e : nodes.entrySet()) {
            e.getValue().callback(key + ">" + e.getKey(), stream);
        }
    }

}
