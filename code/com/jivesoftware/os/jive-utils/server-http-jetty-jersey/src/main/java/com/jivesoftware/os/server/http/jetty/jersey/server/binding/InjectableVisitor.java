package com.jivesoftware.os.server.http.jetty.jersey.server.binding;

/**
 * Interface used to retain type safety when attempting to iterate over a collection
 * of Injectable items.  Thank you, erasure.
 */
public interface InjectableVisitor {

    <T> void visit(Class<T> clazz, T instance);

}
