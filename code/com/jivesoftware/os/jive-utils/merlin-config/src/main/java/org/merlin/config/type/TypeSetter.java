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
import org.merlin.config.ConfigUtil;
import org.merlin.config.Configuration;

/**
 * Superclass for setting a particular configuration type on a configuration source.
 *
 * @author Merlin Hughes
 * @version 0.1, 2007/04/15
 */
public abstract class TypeSetter {

    /**
     * Set the specified configuration property, managing its type as appropriate.
     *
     * @param configuration The configuration source.
     * @param property The property name.
     * @param value The configuration value.
     */
    public abstract void set(ConfigHandler handler, String property, Object value);

    /**
     * Get a TypeSetter instance appropriate for the parameter type of the specified configuration interface method.
     *
     * @param configInterface The configuration interface.
     * @param method The method.
     *
     * @return An appropriate TypeGetter.
     */
    public static TypeSetter getInstance(Class<?> configInterface, Method method) {
        Class<?> methodType = ConfigUtil.getMethodType(method);
        try {
            // If Configuration can get the type then I assume it can set it
            String name = methodType.getSimpleName();
            Configuration.class.getMethod("get" + name, String.class);
            return new DirectTypeSetter();
        } catch (NoSuchMethodException ex) {
            return TypeStringify.getInstance(configInterface, method);
        }
    }

}
