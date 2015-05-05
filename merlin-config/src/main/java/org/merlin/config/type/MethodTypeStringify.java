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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.merlin.config.ConfigHandler;
import org.merlin.config.ConfigurationRuntimeException;

/**
 * A stringifier that converts a type to a string by invoking a method on it and converting the result directly to a string.
 *
 * @author Merlin Hughes
 * @version 0.1, 2007/04/15
 */
public class MethodTypeStringify extends TypeStringify {

    /**
     * The stringify method.
     */
    private Method _method;

    /**
     * Create a new MethodTypeStringify.
     *
     * @param method A method that takes no argument.
     */
    public MethodTypeStringify(Method method) {
        _method = method;
    }

    /* Inherited. */
    @Override
    public String stringify(ConfigHandler handler, Object value) {
        try {
            try {
                return String.valueOf(_method.invoke(value));
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Throwable th) {
            throw new ConfigurationRuntimeException("Type stringify error", th);
        }
    }
}
