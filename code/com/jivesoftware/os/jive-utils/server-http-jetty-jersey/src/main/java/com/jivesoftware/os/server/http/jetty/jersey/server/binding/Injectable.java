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

import com.google.common.base.Preconditions;

/**
 * Holder for object instances which are injectable into the jersey context.
 *
 * @param <T> instance object type
 */
public class Injectable<T> {

    private final Class<T> clazz;
    private final T instance;

    private Injectable(Class<T> clazz, T instance) {
        this.clazz = clazz;
        this.instance = instance;
    }

    public static <T2> Injectable<T2> of(Class<T2> injectableType, T2 instance) {
        Preconditions.checkNotNull(instance);
        return new Injectable<>(injectableType, instance);
    }

    @SuppressWarnings("unchecked")
    public static <T2> Injectable<T2> ofUnsafe(Class<T2> injectableType, Object instance) {
        Preconditions.checkNotNull(injectableType);
        Preconditions.checkNotNull(instance);
        Preconditions.checkArgument(injectableType.isAssignableFrom(instance.getClass()),
            "Injectable must be assignable to type '" + injectableType + "': " + instance);
        return new Injectable<>(injectableType, (T2) instance);
    }

    @SuppressWarnings("unchecked")
    public static <T2> Injectable<T2> of(T2 instance) {
        Preconditions.checkNotNull(instance);
        return new Injectable<>((Class<T2>) instance.getClass(), instance);
    }

    public T getInstance() {
        return instance;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public void visit(InjectableVisitor visitor) {
        visitor.visit(clazz, instance);
    }

}
