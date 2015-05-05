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
package org.merlin.config;

import java.lang.reflect.Method;

/**
 * Implementation of configuration defaults application method.
 *
 * @author Merlin Hughes
 * @version 0.1, 2007/04/15
 */
public class ConfigName extends ConfigMethod {

    /**
     * The configuration interface.
     */
    private final Class<?> configInterface;

    /**
     * Create a new ConfigName instance.
     *
     * @param configInterface The configuration interface.
     * @param method The method.
     */
    @SuppressWarnings("unused")
    public ConfigName(Class<?> configInterface, Method method) {
        this.configInterface = configInterface;
    }

    /**
     * {@inheritDoc} This implementation invokes {@link #name}.
     */
    @Override
    public Object invoke(ConfigHandler handler, Object[] args) {
        return name(handler);
    }

    @SuppressWarnings("unused")
    public String name(ConfigHandler handler) {
        String name = ConfigUtil.getPropertyPrefix(configInterface);
        if (name.endsWith(".")) {
            // removes trailing "."
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }

    /**
     * The ConfigName factory.
     */
    public static final Factory FACTORY = new Factory() {
        /**
         * {@inheritDoc} The method has the name name, String return type and no parameters.
         */
        @Override
        public boolean canHandle(Method method) {
            String methodName = method.getName();
            Class<?> returnType = method.getReturnType();
            Class<?>[] parameterTypes = method.getParameterTypes();
            return "name".equals(methodName)
                && String.class.equals(returnType)
                && (parameterTypes.length == 0);
        }

        /* Inherited. */
        @Override
        public ConfigMethod newInstance(Class<?> configInterface, Method method) {
            return new ConfigName(configInterface, method);
        }
    };
}
