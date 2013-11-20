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
