package com.jivesoftware.os.jive.utils.hwal.read.rcvs;

import com.jivesoftware.os.jive.utils.hwal.read.WALCursorStore;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;

/**
 *
 * @author jonathan
 */
public class RCVSWALCursorStore implements WALCursorStore {

    private final RowColumnValueStore<String, Integer, Long, Long, ? extends Exception> cursors;

    public RCVSWALCursorStore(RowColumnValueStore<String, Integer, Long, Long, ? extends Exception> cursors) {
        this.cursors = cursors;
    }

    @Override
    public void set(String topic, int parition, long offset) {
        //cursors.add(tenant, row, "", offset, Integer.MIN_VALUE, null);
    }

    @Override
    public long get(String topic, int parition) {
        return 0;
    }
}
