package org.apache.log4j.spi;

import org.apache.log4j.Level;

// Stub necessary for EmbeddedHBase to work without log4j
public class LoggingEvent {
    public Level getLevel() {
        throw new UnsupportedOperationException("Stub!");
    }
}
