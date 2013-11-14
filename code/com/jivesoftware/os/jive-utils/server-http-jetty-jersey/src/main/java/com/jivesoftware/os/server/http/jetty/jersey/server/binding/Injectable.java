package com.jivesoftware.os.server.http.jetty.jersey.server.binding;

import com.google.common.base.Preconditions;

/**
 * Holder for object instances which are injectable into the jersey context.
 *
 * @param <T> instance object type
 */
public class Injectable<T> {

    private final Class<T> clazz;
    private final T instance;

    private Injectable(Class<T> clazz, T instance) {
        this.clazz = clazz;
        this.instance = instance;
    }

    public static <T2> Injectable<T2> of(Class<T2> injectableType, T2 instance) {
        Preconditions.checkNotNull(instance);
        return new Injectable<>(injectableType, instance);
    }

    @SuppressWarnings("unchecked")
    public static <T2> Injectable<T2> ofUnsafe(Class<T2> injectableType, Object instance) {
        Preconditions.checkNotNull(injectableType);
        Preconditions.checkNotNull(instance);
        Preconditions.checkArgument(injectableType.isAssignableFrom(instance.getClass()),
            "Injectable must be assignable to type '" + injectableType + "': " + instance);
        return new Injectable<>(injectableType, (T2) instance);
    }

    @SuppressWarnings("unchecked")
    public static <T2> Injectable<T2> of(T2 instance) {
        Preconditions.checkNotNull(instance);
        return new Injectable<>((Class<T2>) instance.getClass(), instance);
    }

    public T getInstance() {
        return instance;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public void visit(InjectableVisitor visitor) {
        visitor.visit(clazz, instance);
    }

}
