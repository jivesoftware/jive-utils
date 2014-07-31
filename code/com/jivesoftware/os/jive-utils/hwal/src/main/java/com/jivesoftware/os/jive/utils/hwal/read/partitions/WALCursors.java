package com.jivesoftware.os.jive.utils.hwal.read.partitions;

import java.util.List;

/**
 *
 * @author jonathan
 */
public class WALCursors {

    private final WALCursorStore cursorStore;

    public WALCursors(WALCursorStore cursorStore) {
        this.cursorStore = cursorStore;
    }
    

    public List<WALCursor> getCursors() {
        return null;
    }

    public int getNumberOfParitions() {
        return 0;
    }
}
