package org.apache.log4j;

import org.apache.log4j.spi.LoggingEvent;

// Stub necessary for EmbeddedHBase to work without log4j
public abstract class AppenderSkeleton {
    public abstract void append(LoggingEvent event);

    public abstract void close();

    public abstract boolean requiresLayout();
}
