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
import org.merlin.config.annotations.Description;
import org.merlin.config.type.TypeStringify;

/**
 * Implementation of configuration defaults application method.
 *
 * @author Merlin Hughes
 * @version 0.1, 2007/04/15
 */
public class ConfigApplyDefaults extends ConfigMethod {

    /**
     * The configuration interface.
     */
    private final Class<?> _configInterface;

    /**
     * Create a new ConfigApplyDefaults instance.
     *
     * @param configInterface The configuration interface.
     * @param method The method.
     */
    @SuppressWarnings("unused")
    public ConfigApplyDefaults(Class<?> configInterface, Method method) {
        _configInterface = configInterface;
    }

    /**
     * {@inheritDoc} This implementation invokes {@link #applyDefaults}.
     */
    @Override
    public Object invoke(ConfigHandler handler, Object[] args) {
        applyDefaults(handler);
        return null;
    }

    /**
     * Apply the defaults for all configuration fields that have a default.
     *
     * @param configuration The configuration source.
     */
    public void applyDefaults(ConfigHandler handler) {
        // Set on the config directly rather than invoke a setter in case there
        // is no setter.
        for (Method getter : _configInterface.getMethods()) {
            String methodName = getter.getName();
            if (ConfigUtil.GET_RE.matcher(methodName).matches() && // is a getter
                (getter.getParameterTypes().length == 0)) {

                String setterName = "set" + methodName.substring(3);
                Method setter = null;
                try {
                    setter = _configInterface.getMethod(setterName, getter.getReturnType());
                } catch (NoSuchMethodException ex) {
                } catch (SecurityException ex) {
                }

                Object defaultValue = ConfigUtil.getDefaultValue(handler, _configInterface, getter);
                if (defaultValue != ConfigUtil.NO_DEFAULT) { // is defaulted
                    Description descriptionAnnotation = ConfigUtil.getAnnotation(_configInterface, getter, Description.class, true);
                    String propertyName = ConfigUtil.getPropertyName(handler, _configInterface, getter);
                    TypeStringify stringify = TypeStringify.getInstance(_configInterface, (setter == null) ? getter : setter);
                    String stringified = stringify.stringify(handler, defaultValue);

                    String description = "";
                    if (descriptionAnnotation != null) {
                        description = descriptionAnnotation.value();
                    }
                    //System.out.println(" applyDefaults:"+propertyName+"=\""+stringified+"\"; //"+description);
                    handler.getConfiguration().setProperty(propertyName, stringified, description);
                }
            }
        }
    }

    /**
     * The ConfigApplyDefaults factory.
     */
    public static final Factory FACTORY = new Factory() {
        /**
         * {@inheritDoc} The method has the name applyDefaults, void return type and no parameters.
         */
        @Override
        public boolean canHandle(Method method) {
            String methodName = method.getName();
            Class<?> returnType = method.getReturnType();
            Class<?>[] parameterTypes = method.getParameterTypes();
            return "applyDefaults".equals(methodName)
                && Void.TYPE.equals(returnType)
                && (parameterTypes.length == 0);
        }

        /* Inherited. */
        @Override
        public ConfigMethod newInstance(Class<?> configInterface, Method method) {
            return new ConfigApplyDefaults(configInterface, method);
        }
    };
}
