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
