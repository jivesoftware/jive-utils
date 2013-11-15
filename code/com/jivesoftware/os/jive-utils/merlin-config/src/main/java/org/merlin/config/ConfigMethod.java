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
import java.util.HashMap;
import java.util.Map;

/**
 * Superclass for all configuration method implementations.
 *
 * @author Merlin Hughes
 * @version 0.1, 2007/04/15
 */
public abstract class ConfigMethod {

    /**
     * Create a new ConfigMethod instance.
     */
    protected ConfigMethod() {
    }

    /**
     * Handle a configuration interface method invocation.
     *
     * @param handler The handler.
     * @param args The method arguments.
     *
     * @return The method result.
     */
    public abstract Object invoke(ConfigHandler handler, Object[] args);

    /**
     * A cache of instantiated configuration method implementations.
     */
    private static final Map<InterfaceAndMethod, ConfigMethod> __configMethods = new HashMap<InterfaceAndMethod, ConfigMethod>();

    /**
     * Get a ConfigMethod implementation appropriate for handling a configuration interface method. Internally, this uses a cache so that it can operate very
     * efficiently.
     *
     * @param configInterface The configuration interface.
     * @param method The method.
     *
     * @return The method implementation.
     */
    public static synchronized ConfigMethod getInstance(Class<?> configInterface, Method method) {
        InterfaceAndMethod key = new InterfaceAndMethod(configInterface, method);
        ConfigMethod configMethod = __configMethods.get(key);
        if (configMethod == null) {
            configMethod = newInstance(configInterface, method);
            __configMethods.put(key, configMethod);
        }
        return configMethod;
    }

    /**
     * Create a new ConfigMethod implementation appropriate for handling a configuration interface method.
     *
     * @param configInterface The configuration interface.
     * @param method The method.
     *
     * @return The method implementation.
     */
    private static ConfigMethod newInstance(Class<?> configInterface, Method method) {
        for (Factory factory : CONFIG_METHOD_FACTORIES) {
            if (factory.canHandle(method)) {
                return factory.newInstance(configInterface, method);
            }
        }
        throw new ConfigurationRuntimeException("Invalid config interface method: " + method);
    }

    /**
     * Supported configuration method factories.
     */
    private static final Factory[] CONFIG_METHOD_FACTORIES = {
        ConfigGetter.FACTORY,
        ConfigSetter.FACTORY,
        ConfigApplyDefaults.FACTORY,
        ConfigName.FACTORY
    };

    /**
     * Interface describing a configuration method factory.
     */
    public static interface Factory {

        /**
         * Test whether this factory can handle a particular configuration interface method.
         *
         * @param method The method.
         *
         * @return Whether this factory can handle the method.
         */
        public boolean canHandle(Method method);

        /**
         * Create a new configuration method implementation to handle a configuration interface method.
         *
         * @param configInterface The configuration interface.
         * @param method The method.
         *
         * @return The method implementation.
         */
        public ConfigMethod newInstance(Class<?> configInterface, Method method);
    }

    private static class InterfaceAndMethod {
        private final Class<?> configInterface;
        private final Method method;

        public InterfaceAndMethod(Class<?> configInterface, Method method) {
            this.configInterface = configInterface;
            this.method = method;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + (this.configInterface != null ? this.configInterface.hashCode() : 0);
            hash = 59 * hash + (this.method != null ? this.method.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final InterfaceAndMethod other = (InterfaceAndMethod) obj;
            if (this.configInterface != other.configInterface && (this.configInterface == null || !this.configInterface.equals(other.configInterface))) {
                return false;
            }
            if (this.method != other.method && (this.method == null || !this.method.equals(other.method))) {
                return false;
            }
            return true;
        }

    }
}
