package com.jivesoftware.os.jive.utils.hwal.shared.api;

/** @author jonathan */
public class WALEntry {

    public final long uniqueOrderingId;
    public final long timestamp;
    public final WALKey key;
    public final WALPayload payload;

    public WALEntry(long uniqueOrderingId, long timestampMillis, WALKey key, WALPayload payload) {
        this.uniqueOrderingId = uniqueOrderingId;
        this.timestamp = timestampMillis;
        this.key = key;
        this.payload = payload;
    }

}
