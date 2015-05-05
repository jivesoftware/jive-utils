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
import java.lang.reflect.InvocationTargetException;
import org.merlin.config.ConfigHandler;
import org.merlin.config.ConfigurationRuntimeException;

/**
 * A factory that builds a type by invoking its constructor, supplying the string value as a parameter.
 *
 * @author Merlin Hughes
 * @version 0.1, 2007/04/15
 */
public class ConstructorTypeFactory extends TypeFactory {

    /**
     * The type constructor.
     */
    private final Constructor<?> _constructor;

    /**
     * Create a new ConstructorTypeFactory.
     *
     * @param constructor A constructor that takes a single string argument.
     */
    public ConstructorTypeFactory(Constructor<?> constructor) {
        _constructor = constructor;
    }

    /* Inherited. */
    @Override
    public Object build(ConfigHandler handler, String string) {
        try {
            try {
                return _constructor.newInstance(string);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Throwable th) {
            throw new ConfigurationRuntimeException("Type construct error", th);
        }
    }
}
