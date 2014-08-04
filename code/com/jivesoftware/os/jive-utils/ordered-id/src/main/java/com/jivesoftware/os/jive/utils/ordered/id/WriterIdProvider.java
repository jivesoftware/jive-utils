package com.jivesoftware.os.jive.utils.ordered.id;

public interface WriterIdProvider {
    WriterId getWriterId() throws OutOfWriterIdsException;
}
