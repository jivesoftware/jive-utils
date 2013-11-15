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
import org.merlin.config.type.TypeSetter;

/**
 * Implementation of configuration setter methods.
 *
 * @author Merlin Hughes
 * @version 0.1, 2007/04/15
 */
public class ConfigSetter extends ConfigMethod {

    private final Class<?> _configInterface;
    private final Method _method;

    /**
     * The type-specific setter.
     */
    private final TypeSetter _typeSetter;

    /**
     * Create a new ConfigSetter instance.
     *
     * @param configInterface The configuration interface.
     * @param method The method.
     */
    public ConfigSetter(Class<?> configInterface, Method method) {
        _configInterface = configInterface;
        _method = method;
        _typeSetter = TypeSetter.getInstance(configInterface, method);
    }

    /**
     * {@inheritDoc} This implementation invokes {@link #setProperty} with the filed value.
     */
    @Override
    public Object invoke(ConfigHandler handler, Object[] args) {
        setProperty(handler, args[0]);
        return null;
    }

    /**
     * Set a configuration field value.
     *
     * @param configuration The configuration source.
     * @param value The field value.
     */
    public void setProperty(ConfigHandler handler, Object value) {
        _typeSetter.set(handler, ConfigUtil.getPropertyName(handler, _configInterface, _method), value);
    }

    /**
     * The ConfigSetter factory.
     */
    public static final Factory FACTORY = new Factory() {
        /**
         * {@inheritDoc} The method has the name set*, has a void return type and one parameter.
         */
        @Override
        public boolean canHandle(Method method) {
            String methodName = method.getName();
            Class<?> returnType = method.getReturnType();
            Class<?>[] parameterTypes = method.getParameterTypes();
            return ConfigUtil.SET_RE.matcher(methodName).matches()
                && Void.TYPE.equals(returnType)
                && (parameterTypes.length == 1);
        }

        /* Inherited. */
        @Override
        public ConfigMethod newInstance(Class<?> configInterface, Method method) {
            return new ConfigSetter(configInterface, method);
        }
    };
}
