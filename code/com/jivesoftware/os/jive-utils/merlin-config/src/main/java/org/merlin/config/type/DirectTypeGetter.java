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
 * Gets a typed configuration property by directly invoking the relevant method on the configuration source.
 *
 * @author Merlin Hughes
 * @version 0.1, 2007/04/15
 */
public class DirectTypeGetter extends TypeGetter {

    /**
     * The type getter method.
     */
    private final Method _method;

    /**
     * Create a new DirectTypeGetter.
     *
     * @param method A configuration getter method.
     */
    public DirectTypeGetter(Method method) {
        _method = method;
    }

    /* Inherited. */
    @Override
    public Object get(ConfigHandler handler, String property) {
        try {
            try {
                return _method.invoke(handler.getConfiguration(), property);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Throwable th) {
            throw new ConfigurationRuntimeException("Type get error", th);
        }
    }
}
