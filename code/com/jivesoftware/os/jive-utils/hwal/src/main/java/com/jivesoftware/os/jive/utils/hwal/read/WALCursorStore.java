package com.jivesoftware.os.jive.utils.hwal.read;

/**
 *
 * @author jonathan
 */
public interface WALCursorStore {

    long get(String cursorGroup, String topicId, int partitionId);

    void set(String cursorGroup, String topicId, int partitionId, long offset);
}
