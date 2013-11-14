package com.jivesoftware.os.server.http.jetty.jersey.endpoints.killswitch;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class KillSwitchService {

    private final ConcurrentHashMap<String, KillSwitch> killSwitches = new ConcurrentHashMap<>();

    /**
     * Gives back provided or existing KillSwitch.
     *
     * @param killSwitch
     * @return
     */
    public KillSwitch add(KillSwitch killSwitch) {
        if (killSwitch == null) {
            return null;
        }
        KillSwitch had = killSwitches.putIfAbsent(killSwitch.getName(), killSwitch);
        if (had != null) {
            return had;
        } else {
            return killSwitch;
        }
    }

    public boolean set(String name, boolean state) {
        KillSwitch got = killSwitches.get(name);
        if (got == null) {
            return false;
        }
        got.getState().set(state);
        return true;
    }

    public Collection<KillSwitch> getAll() {
        return killSwitches.values();
    }
}
