package com.jivesoftware.os.jive.utils.hwal.read.rcvs;

import com.jivesoftware.os.jive.utils.hwal.read.WALCursorStore;
import com.jivesoftware.os.jive.utils.hwal.shared.api.TopicPartition;
import com.jivesoftware.os.jive.utils.id.TenantId;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;

/**
 *
 * @author jonathan
 */
public class RCVSCursorStore implements WALCursorStore {

    private final RowColumnValueStore<TenantId, TopicPartition, Long, Long, ? extends Exception> cursors;

    public RCVSCursorStore(RowColumnValueStore<TenantId, TopicPartition, Long, Long, ? extends Exception> cursors) {
        this.cursors = cursors;
    }

    public void set(TenantId tenant, TopicPartition row, long offset) {
        //cursors.add(tenant, row, "", offset, Integer.MIN_VALUE, null);
    }

    public long get(TenantId tenant, TopicPartition row) {
        return 0;
    }
}
