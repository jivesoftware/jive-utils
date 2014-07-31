package com.jivesoftware.os.jive.utils.hwal.read.partitions;

import com.google.common.base.Optional;
import com.jivesoftware.os.jive.utils.hwal.read.WALCursorStore;
import com.jivesoftware.os.jive.utils.hwal.shared.api.TopicId;
import com.jivesoftware.os.jive.utils.hwal.shared.api.TopicPartition;
import com.jivesoftware.os.jive.utils.id.TenantId;

/**
 *
 * @author jonathan
 */
public class WALCursor {

    private final TenantId tenantId;
    private final TopicId topicId;
    private final PartitionId partitionId;
    private final WALCursorStore cursorStore;

    public WALCursor(TenantId tenantId, TopicId topicId, PartitionId partitionId, WALCursorStore cursorStore) {
        this.tenantId = tenantId;
        this.topicId = topicId;
        this.partitionId = partitionId;
        this.cursorStore = cursorStore;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public TopicId getTopicId() {
        return topicId;
    }
    
    public Optional<Integer> getPartition() {
        return partitionId.getId();
    }

    public Optional<Long> currentOffest() {
        Optional<Integer> id = partitionId.getId();
        if (id.isPresent()) {
            long offset = cursorStore.get(tenantId, new TopicPartition(id.get(), topicId.getId()));
            return Optional.of(offset);
        } else {
            return Optional.absent();
        }
    }

    public boolean commit(long offest) {
        Optional<Integer> id = partitionId.getId();
        if (id.isPresent()) {
            cursorStore.set(tenantId, new TopicPartition(id.get(), topicId.getId()), offest);
            return true;
        } else {
            return false;
        }
    }

}
