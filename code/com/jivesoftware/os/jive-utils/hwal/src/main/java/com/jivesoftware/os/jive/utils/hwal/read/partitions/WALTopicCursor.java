package com.jivesoftware.os.jive.utils.hwal.read.partitions;

import com.google.common.base.Optional;
import com.jivesoftware.os.jive.utils.hwal.read.WALCursorStore;

/**
 *
 * @author jonathan
 */
public class WALTopicCursor {

    private final String topicId;
    private final PartitionId partitionId;
    private final WALCursorStore cursorStore;

    public WALTopicCursor(String topicId, PartitionId partitionId, WALCursorStore cursorStore) {
        this.topicId = topicId;
        this.partitionId = partitionId;
        this.cursorStore = cursorStore;
    }

    @Override
    public String toString() {
        return "WALTopicCursor{" + "topicId=" + topicId + ", partitionId=" + partitionId.getId().or(-1) + '}';
    }


    public String getTopicId() {
        return topicId;
    }

    public Optional<Integer> getPartition() {
        return partitionId.getId();
    }

    public Optional<Long> currentOffest() {
        Optional<Integer> id = partitionId.getId();
        if (id.isPresent()) {
            long offset = cursorStore.get(topicId, id.get());
            return Optional.of(offset);
        } else {
            return Optional.absent();
        }
    }

    public boolean commit(long offest) {
        Optional<Integer> id = partitionId.getId();
        if (id.isPresent()) {
            cursorStore.set(topicId, id.get(), offest);
            return true;
        } else {
            return false;
        }
    }

}
