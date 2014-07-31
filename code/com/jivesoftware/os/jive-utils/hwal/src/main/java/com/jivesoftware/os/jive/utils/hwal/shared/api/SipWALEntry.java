package com.jivesoftware.os.jive.utils.hwal.shared.api;

/**
 * @author jonathan
 */
public class SipWALEntry {

    public final long uniqueOrderingId;
    public final long ingressedTimestampMillis;
    public final WALKey key;

    public SipWALEntry(long uniqueOrderingId, long ingressedTimestampMillis, WALKey key) {
        this.uniqueOrderingId = uniqueOrderingId;
        this.ingressedTimestampMillis = ingressedTimestampMillis;
        this.key = key;
    }
}
