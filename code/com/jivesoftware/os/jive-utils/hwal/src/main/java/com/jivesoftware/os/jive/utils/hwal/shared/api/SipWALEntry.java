package com.jivesoftware.os.jive.utils.hwal.shared.api;

/**
 * @author jonathan
 */
public class SipWALEntry {

    public final long uniqueOrderingId;
    public final long ingressedTimestampMillis;
    public final byte[] key;

    public SipWALEntry(long uniqueOrderingId, long ingressedTimestampMillis, byte[] key) {
        this.uniqueOrderingId = uniqueOrderingId;
        this.ingressedTimestampMillis = ingressedTimestampMillis;
        this.key = key;
    }

    @Override
    public String toString() {
        return "SipWALEntry{" + "uniqueOrderingId=" + uniqueOrderingId + ", ingressedTimestampMillis=" + ingressedTimestampMillis + ", key=" + key + '}';
    }

}
