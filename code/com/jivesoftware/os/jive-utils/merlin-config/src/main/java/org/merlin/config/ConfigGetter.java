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
import java.util.NoSuchElementException;
import org.merlin.config.type.TypeGetter;

/**
 * Implementation of configuration getter methods.
 *
 * @author Merlin Hughes
 * @version 0.1, 2007/04/15
 */
public class ConfigGetter extends ConfigMethod {

    private final Class<?> _configInterface;
    private final Method _method;

    /**
     * The type-specific getter.
     */
    private final TypeGetter _typeGetter;

    /**
     * Create a new ConfigGetter instance.
     *
     * @param configInterface The configuration interface.
     * @param method The method.
     */
    public ConfigGetter(Class<?> configInterface, Method method) {
        _configInterface = configInterface;
        _method = method;
        _typeGetter = TypeGetter.getInstance(configInterface, method);
        // not needed for defaulted method, but no harm.
    }

    /**
     * {@inheritDoc} This implementation invokes either {@link #getProperty(ConfigHandler)} or {@link #getProperty(ConfigHandler,Object)} depending on whether a
     * run-time default value was specified.
     */
    @Override
    public Object invoke(ConfigHandler handler, Object[] args) {
        if ((args == null) || (args.length == 0)) {
            return getProperty(handler);
        } else {
            return getProperty(handler, args[0]);
        }
    }

    /**
     * Get a configuration field value.
     *
     * @param handler The configuration source.
     *
     * @return The field value.
     */
    public Object getProperty(ConfigHandler handler) {
        Object defaultValue = ConfigUtil.getDefaultValue(handler, _configInterface, _method);
        Object value = getProperty(handler, defaultValue);
        if (value == ConfigUtil.NO_DEFAULT) {
            throw new ConfigurationRuntimeException("Unable to retrieve configuration value of type " + _method.getReturnType()
                + " for " + _method.getDeclaringClass().getSimpleName() + "#" + _method.getName() +
                ". No value was found, and no default was specified. [Method: " + _method + "]");

        }
        return value;
    }

    /**
     * Get a configuration field value with a run-time default.
     *
     * @param handler The configuration source.
     * @param defaultValue The default value to use if a value is not specified by the configuration source.
     *
     * @return The field value.
     */
    public Object getProperty(ConfigHandler handler, Object defaultValue) {
        try {
            Object value = _typeGetter.get(handler, ConfigUtil.getPropertyName(handler, _configInterface, _method));
            return value != null ? value : defaultValue;
        } catch (NoSuchElementException ex) { // primitives
            if (defaultValue == ConfigUtil.NO_DEFAULT) {
                throw ex;
            }
            return defaultValue;
        }
    }

    /**
     * The ConfigGetter factory.
     */
    public static final Factory FACTORY = new Factory() {
        /**
         * {@inheritDoc} The method has the name get* or is* (if the return type is boolean), has a non-void return type and either no parameters or one
         * parameter with the same type as the return value.
         */
        @Override
        public boolean canHandle(Method method) {
            String methodName = method.getName();
            Class<?> returnType = method.getReturnType();
            Class<?>[] parameterTypes = method.getParameterTypes();
            return ConfigUtil.GET_RE.matcher(methodName).matches()
                && !Void.TYPE.equals(returnType)
                && ((Boolean.TYPE.equals(returnType) || Boolean.class.equals(returnType)) || methodName.startsWith("get"))
                && ((parameterTypes.length == 0)
                || ((parameterTypes.length == 1) && parameterTypes[0].equals(returnType)));
        }

        /* Inherited. */
        @Override
        public ConfigMethod newInstance(Class<?> configInterface, Method configMethod) {
            return new ConfigGetter(configInterface, configMethod);
        }
    };
}
