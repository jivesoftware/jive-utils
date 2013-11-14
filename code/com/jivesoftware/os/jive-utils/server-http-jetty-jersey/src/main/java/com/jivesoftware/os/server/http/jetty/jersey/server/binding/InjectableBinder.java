package com.jivesoftware.os.server.http.jetty.jersey.server.binding;

import java.util.List;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class InjectableBinder extends AbstractBinder {
    private final List<Injectable<?>> injectables;

    public InjectableBinder(List<Injectable<?>> injectables) {
        this.injectables = injectables;
    }

    @Override
    protected void configure() {
        for (Injectable<?> injectable : injectables) {
            injectable.visit(new InjectableVisitor() {
                @Override
                public <T> void visit(Class<T> clazz, T instance) {
                    bind(instance).to(clazz);
                }
            });
        }
    }
}
