package com.jivesoftware.os.server.http.jetty.jersey.endpoints.killswitch;

import com.google.common.base.Preconditions;
import java.util.concurrent.atomic.AtomicBoolean;

public class KillSwitch {

    private final String name;
    private final AtomicBoolean state;

    public KillSwitch(String name, AtomicBoolean state) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(state);
        this.name = name;
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public AtomicBoolean getState() {
        return state;
    }

    @Override
    public String toString() {
        return "KillSwitch{" + "name=" + name + ", state=" + state + '}';
    }
}
