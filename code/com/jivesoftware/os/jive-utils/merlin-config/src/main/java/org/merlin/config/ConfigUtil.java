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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.merlin.config.annotations.AbsoluteProperty;
import org.merlin.config.annotations.Prefix;
import org.merlin.config.annotations.Property;
import org.merlin.config.defaults.BooleanDefault;
import org.merlin.config.defaults.ByteDefault;
import org.merlin.config.defaults.ClassDefault;
import org.merlin.config.defaults.Default;
import org.merlin.config.defaults.DoubleDefault;
import org.merlin.config.defaults.FloatDefault;
import org.merlin.config.defaults.IntDefault;
import org.merlin.config.defaults.LongDefault;
import org.merlin.config.defaults.ShortDefault;
import org.merlin.config.defaults.StringDefault;
import org.merlin.config.type.TypeFactory;

/**
 * Various configuration utility methods.
 *
 * @author Merlin Hughes
 * @version 0.1, 2007/04/15
 */
public class ConfigUtil {

    /**
     * Prohibited.
     */
    private ConfigUtil() {
    }

    /**
     * Regex matching a getter (is* or get*). The second group is the field name.
     */
    static final Pattern GET_RE = Pattern.compile("^(is|get)(.+)$");
    /**
     * Regex matching a setter (set*). The second group is the field name.
     */
    static final Pattern SET_RE = Pattern.compile("^(set)(.+)$");
    /**
     * Regex matching an accessor (is*, get* or set*). The second group is the field name.
     */
    static final Pattern ACCESS_RE = Pattern.compile("^(is|get|set)(.+)$");
    /**
     * Regex matching an add listener method (add*Listener). The second group is the field name.
     */
    static final Pattern ADD_LISTENER_RE = Pattern.compile("^(add)(.*)Listener$");
    /**
     * Regex matching a remove listener method (remove*Listener). The second group is the field name.
     */
    static final Pattern REMOVE_LISTENER_RE = Pattern.compile("^(remove)(.*)Listener$");
    /**
     * Regex matching a listener method (add*Listener or remove*Listener). The second group is the field name.
     */
    static final Pattern LISTENER_RE = Pattern.compile("^(add|remove)(.*)Listener$");

    /**
     * Uncapitalize a string with support for leading acronyms. This supports pretty uncapitalization of strings such as "URL" (to "url") and "URLDecoder" (to
     * "urlDecoder"). If a string begins with a sequence of capital letters, all but the last are uncapitalized, except in the case that the entire string is
     * capitalized or the capitals are followed by a non-letter, in which case all are uncapitalized.
     *
     * @param str The string.
     *
     * @return The uncapitalized string.
     */
    public static String extendedUncapitalize(String str) {
        // fooBar -> fooBar
        // FooBar -> fooBar
        // FOOBar -> fooBar
        // FOOBAR -> foobar
        // FOO8ar -> foo8ar
        int index = 0;
        int length = str.length();
        while ((index < length)
            && Character.isUpperCase(str.charAt(index))
            && ((index == 0) || (index == length - 1)
            || !Character.isLowerCase(str.charAt(index + 1)))) {
            ++index;
        }
        return str.substring(0, index).toLowerCase() + str.substring(index);
    }

    /**
     * Get the property name associated with a configuration interface method. If a {@link Property} annotation is present, that value is returned. Otherwise
     * the {@link #getPropertyPrefix interface prefix} is concatenated with the {@link #extendedUncapitalize uncapitalized} property name.
     *
     * @param configInterface The configuration interface.
     * @param method The method.
     *
     * @return The property name.
     */
    public static String getPropertyName(ConfigHandler handler, Class<?> configInterface, Method method) {
        //getPropertyPrefix(configInterface);
        String prefix = "";
        String instanceName = handler.getInstanceName();
        if (method == null) {
            if (instanceName != null && instanceName.length() > 0) {
                return prefix + instanceName + ".";
            } else {
                return prefix;
            }
        }
        AbsoluteProperty absoluteProperty = getAnnotation(configInterface, method, AbsoluteProperty.class, false);
        if (absoluteProperty != null) {
            if (instanceName != null && instanceName.length() > 0) {
                return instanceName + "." + absoluteProperty.value();
            } else {
                return absoluteProperty.value();
            }
        }
        Property property = getAnnotation(configInterface, method, Property.class, false);
        if (property != null) {
            if (instanceName != null && instanceName.length() > 0) {
                return prefix + instanceName + "." + property.value();
            } else {
                return prefix + property.value();
            }
        } else {
            String methodName = method.getName();
            Matcher matcher = ACCESS_RE.matcher(methodName);
            boolean matches = matcher.matches();
            if (!matches) {
                matcher = LISTENER_RE.matcher(methodName);
                matches = matcher.matches();
            }

            if (matches && !"".equals(matcher.group(2))) {
                String prop = matcher.group(2);
                if (instanceName != null && instanceName.length() > 0) {
                    return prefix + instanceName + "." + extendedUncapitalize(prop);
                } else {
                    return prefix + extendedUncapitalize(prop);
                }
            } else {
                throw new IllegalArgumentException("Unsupported method name: " + method);
            }
        }
    }

    /**
     * Get the prefix associated with a configuration interface. If a {@link Prefix} annotation is present, that value is returned. Otherwise, if the interface
     * has an enclosing class, the fully-qualified name of that class is used, or else the name of the package containing the interface. In either of the latter
     * two cases, a '.' is appended to the name.
     *
     * @param configInterface The configuration interface.
     *
     * @return The interface prefix.
     */
    public static String getPropertyPrefix(Class<?> configInterface) {
        // Foo -> ""
        // foo.Bar -> "foo."
        // foo.Bar$Baz -> "foo.Bar."
        StringBuilder sb = new StringBuilder();
        Prefix prefix = configInterface.getAnnotation(Prefix.class);
        if (prefix != null) {
            String p = prefix.value();
            if (p != null && p.length() > 0) {
                sb.append(p);
                sb.append(".");
            }
        }
        sb.append(configInterface.getSimpleName());
        if (sb.length() > 0) {
            if (sb.charAt(sb.length() - 1) != '.') {
                sb.append('.');
            }
        }

        return sb.toString();
    }

    /**
     * Get the type of a method. If the method has a non-void return type, that type is returned. Otherwise if the method has at least one parameter, the type
     * of the first parameter is returned.
     *
     * @param method The method.
     *
     * @return The method type, or else {@link Void#TYPE}.
     */
    public static Class<?> getMethodType(Method method) {
        Class<?> methodType = method.getReturnType();
        if (Void.TYPE.equals(methodType)) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length > 0) {
                methodType = parameterTypes[0];
            }
        }
        return methodType;
    }

    /**
     * Get the plain get method associated with a configuration interface method. This is the unparameterized getter associated with the same field as the
     * specified configuration method. For example, isDisabled() might be returned for the method addDisabledListener().
     *
     * @param configInterface The configuration interface.
     * @param method The method.
     *
     * @return The plain get method, or else null.
     */
    public static Method getGetMethod(Class<?> configInterface, Method method) {
        Method getMethod = null;
        String methodName = method.getName();
        Matcher matcher = ACCESS_RE.matcher(methodName);
        boolean matches = matcher.matches();
        if (!matches) {
            matcher = LISTENER_RE.matcher(methodName);
            matches = matcher.matches();
        }

        if (matches) {
            String prop = matcher.group(2);
            try {
                getMethod = configInterface.getMethod("get" + prop);
            } catch (NoSuchMethodException ex) {
                Class<?> methodType = getMethodType(method);
                if (Boolean.TYPE.equals(methodType)) {
                    try {
                        getMethod = configInterface.getMethod("is" + prop);
                    } catch (NoSuchMethodException ex2) {
                        // ignore
                    }
                }
            }
        }
        return getMethod;
    }

    /**
     * Search for an annotation on a configuration interface method. In addition to searching the method itself, the {@link #getGetMethod
     * plain get method} is also searched, as can the {@link
     * #getMethodType method type} be.
     *
     * @param configInterface The configuration interface.
     * @param method The method.
     * @param annotationType The annotation type of interest.
     * @param searchMethodType Whether to search the method type.
     *
     * @return The annotation, or null.
     */
    public static <T extends Annotation> T getAnnotation(Class<?> configInterface, Method method, Class<T> annotationType, boolean searchMethodType) {
        T annotation = method.getAnnotation(annotationType);
        if (annotation == null) {
            Method getMethod = getGetMethod(configInterface, method);
            if (getMethod != null) {
                annotation = getMethod.getAnnotation(annotationType);
            }
            if ((annotation == null) && searchMethodType) {
                String methodName = method.getName();
                if (ACCESS_RE.matcher(methodName).matches()) {
                    // Is the annotation present on the method type?
                    Class<?> methodType = getMethodType(method);
                    annotation = methodType.getAnnotation(annotationType);
                }
            }
        }
        return annotation;
    }

    /**
     * The value indicating that no default was specified.
     */
    public static final Object NO_DEFAULT = new Object();

    /**
     * Get the default value of a configuration interface method. If a {@link Default} annotation is present then that string is converted to the appropriate
     * type using the {@link TypeFactory} class. Otherwise, for the type Foo, this searches for a FooDefault annotation. If such an annotation is present then
     * its value is returned.
     *
     * @param configInterface The configuration interface.
     * @param method The method.
     *
     * @return The default value, or null.
     */
    public static Object getDefaultValue(ConfigHandler handler, Class<?> configInterface, Method method) {
        // TODO: returnType.cast()?
        try {
            Default defaultValue = method.getAnnotation(Default.class);
            if (defaultValue != null) {
                TypeFactory factory = TypeFactory.getInstance(configInterface, method);
                String string = defaultValue.value();
                return factory.build(handler, string);
            } else {
                Class<?> type = method.getReturnType();
                Annotation annotation = method.getAnnotation(getDefaultAnnotation(type));
                if (annotation != null) {
                    Method valueMethod = annotation.getClass().getMethod("value");
                    return valueMethod.invoke(annotation);
                }
            }
            return NO_DEFAULT;
        } catch (Exception ex) {
            throw new ConfigurationRuntimeException("Default value error", ex);
        }
    }

    private static Class<? extends Annotation> getDefaultAnnotation(Class<?> type) {
        if (Boolean.TYPE.equals(type) || Boolean.class.equals(type)) {
            return BooleanDefault.class;
        } else if (Byte.TYPE.equals(type) || Byte.class.equals(type)) {
            return ByteDefault.class;
        } else if (Double.TYPE.equals(type) || Double.class.equals(type)) {
            return DoubleDefault.class;
        } else if (Float.TYPE.equals(type) || Float.class.equals(type)) {
            return FloatDefault.class;
        } else if (Integer.TYPE.equals(type) || Integer.class.equals(type)) {
            return IntDefault.class;
        } else if (Long.TYPE.equals(type) || Long.class.equals(type)) {
            return LongDefault.class;
        } else if (Short.TYPE.equals(type) || Short.class.equals(type)) {
            return ShortDefault.class;
        } else if (Class.class.equals(type)) {
            return ClassDefault.class;
        } else {
            return StringDefault.class;
        }
    }
}
