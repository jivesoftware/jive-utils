package com.jivesoftware.os.jive.utils.logger;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;

public class MetricLogEvent extends LoggingEvent {

    private final String[] tags;

    public MetricLogEvent(String fqnOfCategoryClass, Category logger,
            Priority level, String[] tags, Object message, Throwable throwable) {
        super(fqnOfCategoryClass, logger, level, message, throwable);
        this.tags = tags;
    }

    public String[] getTags() {
        return tags;
    }
}
