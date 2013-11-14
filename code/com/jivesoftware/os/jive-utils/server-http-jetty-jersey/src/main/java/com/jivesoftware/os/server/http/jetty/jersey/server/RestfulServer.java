package com.jivesoftware.os.server.http.jetty.jersey.server;

import com.jivesoftware.os.jive.utils.base.service.ServiceHandle;
import java.lang.management.ManagementFactory;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class RestfulServer implements ServiceHandle {

    // Copying the default behavior inside jetty
    private static final int ACCEPTORS = Math.max(1, (Runtime.getRuntime().availableProcessors()) / 2);
    private static final int SELECTORS = Runtime.getRuntime().availableProcessors();

    private static final int MIN_THREADS = 8;
    private static final int IDLE_TIMEOUT = 60000;

    private static Server makeServer(final int maxNumberOfThreads, final int maxQueuedRequests) {
        final int maxThreads = maxNumberOfThreads + ACCEPTORS + SELECTORS;
        final BlockingArrayQueue<Runnable> queue = new BlockingArrayQueue<>(MIN_THREADS, MIN_THREADS, maxQueuedRequests);
        return new Server(new QueuedThreadPool(maxThreads, MIN_THREADS, IDLE_TIMEOUT, queue));
    }

    private final Server server;
    private final String applicationName;

    private final ContextHandlerCollection handlers;

    public RestfulServer(int port, String applicationName, int maxNumberOfThreads, int maxQueuedRequests) {
        this.applicationName = applicationName;
        this.server = makeServer(maxNumberOfThreads, maxQueuedRequests);
        this.handlers = new ContextHandlerCollection();

        server.addEventListener(new MBeanContainer(ManagementFactory.getPlatformMBeanServer()));
        server.setHandler(handlers);
        server.addConnector(makeConnector(port));
    }

    private Connector makeConnector(int port) {
        ServerConnector connector = new ServerConnector(server, ACCEPTORS, SELECTORS);
        connector.setPort(port);
        return connector;
    }

    public void addContextHandler(String context, HasServletContextHandler contextHandler) {
        if (context == null || contextHandler == null) { // allows nulls to be ignored which works better with a chaining builder pattern
            return;
        }
        handlers.addHandler(contextHandler.getHandler(server, context, applicationName));
    }

    @Override
    public void start() throws Exception {
        server.start();
    }

    @Override
    public void stop() throws Exception {
        server.stop();
    }
}
