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
package com.jivesoftware.os.server.http.jetty.jersey.server.binding;

import java.util.List;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class InjectableBinder extends AbstractBinder {
    private final List<Injectable<?>> injectables;

    public InjectableBinder(List<Injectable<?>> injectables) {
        this.injectables = injectables;
    }

    @Override
    protected void configure() {
        for (Injectable<?> injectable : injectables) {
            injectable.visit(new InjectableVisitor() {
                @Override
                public <T> void visit(Class<T> clazz, T instance) {
                    bind(instance).to(clazz);
                }
            });
        }
    }
}
