package com.jivesoftware.os.jive.utils.hwal.read.rcvs;

import com.jivesoftware.os.jive.utils.hwal.read.WALCursorStore;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;

/**
 *
 * @author jonathan
 */
public class RCVSWALCursorStore implements WALCursorStore {

    private final RowColumnValueStore<String, String, Integer, Long, ? extends RuntimeException> cursors;

    public RCVSWALCursorStore(RowColumnValueStore<String, String, Integer, Long, ? extends RuntimeException> cursors) {
        this.cursors = cursors;
    }

    @Override
    public void set(String readerGroup, String topic, int parition, long offset) {
        cursors.add(readerGroup, topic, parition, offset, null, null);
    }

    @Override
    public long get(String readerGroup, String topic, int parition) {
        Long got = cursors.get(readerGroup, topic, parition, null, null);
        if (got == null) {
            got = 0L;
        }
        return got;
    }
}
