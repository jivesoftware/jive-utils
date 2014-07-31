package com.jivesoftware.os.jive.utils.hwal.read;

import com.jivesoftware.os.jive.utils.hwal.shared.api.TopicPartition;
import com.jivesoftware.os.jive.utils.id.TenantId;

/**
 *
 * @author jonathan
 */
public interface WALCursorStore {

    long get(TenantId tenant, TopicPartition row);

    void set(TenantId tenant, TopicPartition row, long offset);
}
