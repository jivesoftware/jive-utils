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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Configuration interface method handler.
 *
 * @author Merlin Hughes
 * @version 0.1, 2007/04/15
 */
public class ConfigHandler implements InvocationHandler {

    /**
     * The configuration source.
     */
    private final Configuration _configuration;
    /**
     * The configuration interface.
     */
    private final Class<?> _configInterface;

    private final String _instanceName;

    private final ObjectStringMapper _objectStringMapper;

    /**
     * Create a new ConfigHandler instance.
     *
     * @param configuration The configuration source.
     * @param configInterface The configuration interface.
     */
    public ConfigHandler(Configuration configuration, Class<?> configInterface, String instanceName, ObjectStringMapper objectStringMapper) {
        _configuration = configuration;
        _configInterface = configInterface;
        _instanceName = instanceName;
        _objectStringMapper = objectStringMapper;
    }

    /**
     * Get the configuration source.
     *
     * @return The configuration source.
     */
    public Configuration getConfiguration() {
        return _configuration;
    }

    /**
     * Get the configuration interface.
     *
     * @return The configuration interface.
     */
    public Class<?> getConfigInterface() {
        return _configInterface;
    }

    public String getInstanceName() {
        return _instanceName;
    }

    public ObjectStringMapper getObjectStringMapper() {
        if (_objectStringMapper == null) {
            throw new IllegalStateException("There is no registered objectStringMapper. Please set BindInterfaceToConfiguration.setObjectStringMapper().");
        }
        return _objectStringMapper;
    }

    /**
     * Handle a configuration interface method invocation.
     *
     * @param proxy The proxy implementation.
     * @param method The method being invoked.
     * @param args The method arguments.
     *
     * @return The method result.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        ConfigMethod configMethod = ConfigMethod.getInstance(_configInterface, method);
        return configMethod.invoke(this, args);
    }
}
