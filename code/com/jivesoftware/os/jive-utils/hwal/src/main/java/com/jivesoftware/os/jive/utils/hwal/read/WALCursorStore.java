package com.jivesoftware.os.jive.utils.hwal.read;

/**
 *
 * @author jonathan
 */
public interface WALCursorStore {

    long get(String readerGroup, String topicId, int partitionId);

    void set(String readerGroup, String topicId, int partitionId, long offset);
}
