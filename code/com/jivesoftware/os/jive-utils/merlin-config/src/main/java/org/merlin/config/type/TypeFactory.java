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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import org.merlin.config.ConfigHandler;
import org.merlin.config.ConfigUtil;
import org.merlin.config.ConfigurationRuntimeException;
import org.merlin.config.annotations.Factory;

/**
 * A class that gets a configuration type by retrieving a string value from the configuration and then building the appropriate type from that string.
 *
 * @author Merlin Hughes
 * @version 0.1, 2007/04/15
 */
public abstract class TypeFactory extends TypeGetter {

    private static final Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<Class<?>, Class<?>>();
    static {
        primitiveWrapperMap.put(Boolean.TYPE, Boolean.class);
        primitiveWrapperMap.put(Byte.TYPE, Byte.class);
        primitiveWrapperMap.put(Character.TYPE, Character.class);
        primitiveWrapperMap.put(Short.TYPE, Short.class);
        primitiveWrapperMap.put(Integer.TYPE, Integer.class);
        primitiveWrapperMap.put(Long.TYPE, Long.class);
        primitiveWrapperMap.put(Double.TYPE, Double.class);
        primitiveWrapperMap.put(Float.TYPE, Float.class);
        primitiveWrapperMap.put(Void.TYPE, Void.TYPE);
    }

    /**
     * {@inheritDoc} This implementation retrieves a string value from the configuration source and then invokes {@link #build} to convert the string into the
     * appropriate type.
     */
    @Override
    public Object get(ConfigHandler handler, String property) {
        String string = handler.getConfiguration().getProperty(property);
        return (string == null) ? null : build(handler, string);
    }

    /**
     * Build an instance of the configuration type from a string.
     *
     * @param string The string value.
     *
     * @return An instance of the type.
     */
    public abstract Object build(ConfigHandler handler, String string);

    /**
     * Standard static factory methods to look for.
     */
    private static final String[] FACTORY_METHOD_NAMES = {
        "valueOf",
        "getInstance",
        "parse", // Level
        "forName" // Class
    };

    /**
     * Get a TypeFactory instance appropriate for the return type of the specified configuration interface method.
     *
     * @param configInterface The configuration interface.
     * @param method The method.
     *
     * @return An appropriate TypeFactory.
     *
     * @exception IllegalArgumentException If the type is not supported.
     */
    public static TypeFactory getInstance(Class<?> configInterface, Method method) {
        Class<?> returnType = method.getReturnType();
        if (returnType.isPrimitive()) {
            returnType = primitiveToWrapper(returnType);
        }
        Factory factory = ConfigUtil.getAnnotation(configInterface, method, Factory.class, true);
        if (factory != null) {
            try {
                if ("".equals(factory.method())) {
                    Class<? extends TypeFactory> f = factory.factory();
                    Constructor<?> ctor = f.getConstructor(Class.class);
                    if (Modifier.isPublic(ctor.getModifiers())) {
                        return (TypeFactory) ctor.newInstance(returnType);
                    }
                    return factory.factory().newInstance();
                } else {
                    String methodName = factory.method();
                    Method factoryMethod = returnType.getMethod(methodName, String.class);
                    if (!Modifier.isStatic(factoryMethod.getModifiers())
                        || !Modifier.isPublic(factoryMethod.getModifiers())
                        || !returnType.isAssignableFrom(factoryMethod.getReturnType())) {
                        throw new Exception("Invalid factory method: " + factoryMethod);
                    }
                    return new StaticTypeFactory(factoryMethod);
                }
            } catch (Exception ex) {
                throw new ConfigurationRuntimeException("Type factory error", ex);
            }
        } else {
            for (String methodName : FACTORY_METHOD_NAMES) {
                try {
                    Method factoryMethod = returnType.getMethod(methodName, String.class);
                    if (Modifier.isStatic(factoryMethod.getModifiers())
                        && Modifier.isPublic(factoryMethod.getModifiers())
                        && returnType.isAssignableFrom(factoryMethod.getReturnType())) {
                        return new StaticTypeFactory(factoryMethod);
                    }
                } catch (NoSuchMethodException ex) {
                    // ignored
                }
            }
            try {
                Constructor<?> ctor = returnType.getConstructor(String.class);
                if (Modifier.isPublic(ctor.getModifiers())) {
                    return new ConstructorTypeFactory(ctor);
                }
            } catch (NoSuchMethodException ex) {
                // ignored
            }
            throw new IllegalArgumentException("Unsupported return type: " + method);
        }
    }

    private static Class<?> primitiveToWrapper(Class<?> cls) {
        Class<?> convertedClass = cls;
        if (cls != null && cls.isPrimitive()) {
            convertedClass = primitiveWrapperMap.get(cls);
        }
        return convertedClass;
    }
}
