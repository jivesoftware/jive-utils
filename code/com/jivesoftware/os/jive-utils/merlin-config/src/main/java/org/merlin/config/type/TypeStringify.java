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
import org.merlin.config.ConfigHandler;
import org.merlin.config.ConfigUtil;
import org.merlin.config.ConfigurationRuntimeException;
import org.merlin.config.annotations.Stringify;

/**
 * A class that sets a configuration type by converting the type to a string and then setting the configuration property.
 *
 * @author Merlin Hughes
 * @version 0.1, 2007/04/15
 */
public abstract class TypeStringify extends TypeSetter {

    /**
     * {@inheritDoc} This implementation invokes {@link #stringify} to convert the type into a string and then sets the configuration property to that string.
     */
    @Override
    public void set(ConfigHandler handler, String property, Object value) {
        String string = stringify(handler, value);
        handler.getConfiguration().setProperty(property, string);
    }

    /**
     * Convert an instance of the configuration type into a string.
     *
     * @param value The configuration value.
     *
     * @return A string representation.
     */
    public abstract String stringify(ConfigHandler handler, Object value);

    /**
     * Get a TypeStringify instance appropriate for the parameter type of the specified configuration interface method.
     *
     * @param configInterface The configuration interface.
     * @param method The method.
     *
     * @return An appropriate TypeStringify.
     *
     * @exception IllegalArgumentException If the type is not supported.
     */
    public static TypeStringify getInstance(Class<?> configInterface, Method method) {
        Class<?> methodType = ConfigUtil.getMethodType(method);
        try {
            TypeStringify stringifier = null;
            String methodName = null;
            Stringify stringify = ConfigUtil.getAnnotation(configInterface, method, Stringify.class, true);
            if (stringify != null) {
                if ("".equals(stringify.method())) {
                    Class<? extends TypeStringify> s = stringify.stringify();
                    Constructor<?> ctor = s.getConstructor(Class.class);
                    if (Modifier.isPublic(ctor.getModifiers())) {
                        stringifier = (TypeStringify) ctor.newInstance(methodType);
                    } else {
                        stringifier = s.newInstance();
                    }
                } else {
                    methodName = stringify.method();
                }
            }
            if (methodType.isPrimitive()) {
                stringifier = new PrimativeTypeStringify();
            }
            if (stringifier == null) {

                if (methodName == null) {
                    if (methodType.isEnum()) { // enum
                        methodName = "name";
                    } else if (Class.class.equals(methodType)) { // Class
                        methodName = "getName";
                    } else { // all else
                        methodName = "toString";
                    }
                }
                Method stringifyMethod = methodType.getMethod(methodName);
                if (Modifier.isStatic(stringifyMethod.getModifiers())
                    || !Modifier.isPublic(stringifyMethod.getModifiers())
                    || Void.TYPE.equals(stringifyMethod.getReturnType())
                    || (stringifyMethod.getParameterTypes().length > 0)) {
                    throw new Exception("Invalid stringify method: " + stringifyMethod);
                }
                stringifier = new MethodTypeStringify(stringifyMethod);

            }
            //System.out.println("method:"+method.getName()+" "+stringifier.getClass());
            return stringifier;
        } catch (Exception ex) {
            throw new ConfigurationRuntimeException("Type stringify error", ex);
        }
    }
}
