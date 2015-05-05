/*
 * A High-Level Framework for Application Configuration
 *
 * Copyright 2007 Merlin Hughes / Learning Objects, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.merlin.config.type;

import java.lang.reflect.Method;
import org.merlin.config.ConfigHandler;
import org.merlin.config.Configuration;

/**
 * Superclass for getting a particular configuration type from a configuration source.
 *
 * @author Merlin Hughes
 * @version 0.1, 2007/04/15
 */
public abstract class TypeGetter {

    /**
     * Get the specified configuration property, returning a value of the appropriate type.
     *
     * @param configuration The configuration source.
     * @param property The property name.
     *
     * @return The configuration value, translated to the appropriate type.
     */
    public abstract Object get(ConfigHandler handler, String property);

    /**
     * Get a TypeGetter instance appropriate for the return type of the specified configuration interface method.
     *
     * @param configInterface The configuration interface.
     * @param method The method.
     *
     * @return An appropriate TypeGetter.
     */
    public static TypeGetter getInstance(Class<?> configInterface, Method method) {
        Class<?> returnType = method.getReturnType();
        try {
            String name = returnType.getSimpleName();
            Method getMethod = Configuration.class.getMethod("get" + name, String.class);
            // TODO: throw if !returnType.equals(getMethod.getReturnType()) ???
            return new DirectTypeGetter(getMethod);
        } catch (NoSuchMethodException ex) {
            // return Enum -> Enum.valueOf (Configuration.getString)
            return TypeFactory.getInstance(configInterface, method);
        }
    }
}
