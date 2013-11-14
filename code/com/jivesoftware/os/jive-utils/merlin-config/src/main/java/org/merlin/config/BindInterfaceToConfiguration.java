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
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * The main entry point to the configuration interface framework.
 *
 * @author Merlin Hughes
 * @version 0.1, 2007/04/15
 * @param <T>
 */
public class BindInterfaceToConfiguration<T extends Config> {

    private final Configuration configuration;
    private final Class<T> configInterface;
    private String instanceName = null;

    public BindInterfaceToConfiguration(Class<T> configInterface) {
        this.configuration = new MapBackConfiguration(new HashMap<String, String>());
        this.configInterface = configInterface;
    }

    public BindInterfaceToConfiguration(Configuration configuration, Class<T> configInterface) {
        this.configuration = configuration;
        this.configInterface = configInterface;
    }

    public BindInterfaceToConfiguration<T> setInstanceName(String instanceName) {
        this.instanceName = instanceName;
        return this;
    }

    public T bind() {
        ClassLoader classLoader = configInterface.getClassLoader();
        Class<?>[] interfaces = { configInterface };
        InvocationHandler handler = new ConfigHandler(configuration, configInterface, instanceName);
        Object proxy = Proxy.newProxyInstance(classLoader, interfaces, handler);
        return configInterface.cast(proxy);
    }

    public static final <T extends Config> T bindDefault(Class<T> configurationInterfaceClass) {
        Map<String, String> expected = new HashMap<>();
        T config = new BindInterfaceToConfiguration<>(new MapBackConfiguration(expected), configurationInterfaceClass)
            .bind();
        config.applyDefaults();
        return config;
    }

}
