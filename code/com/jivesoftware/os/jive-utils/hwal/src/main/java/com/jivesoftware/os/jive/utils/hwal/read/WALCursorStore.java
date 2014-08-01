package com.jivesoftware.os.jive.utils.hwal.read;

/**
 *
 * @author jonathan
 */
public interface WALCursorStore {

    long get(String topicId, int partitionId);

    void set(String topicId, int partitionId, long offset);
}
