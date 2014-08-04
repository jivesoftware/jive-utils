package com.jivesoftware.os.jive.utils.hwal.shared.api;

/**
 * @author jonathan
 */
public class WALEntry {

    public final SipWALEntry sipWALEntry;
    public final byte[] payload;

    public WALEntry(SipWALEntry sipWALEntry, byte[] payload) {
        this.sipWALEntry = sipWALEntry;
        this.payload = payload;
    }

    public SipWALEntry getSipWALEntry() {
        return sipWALEntry;
    }

    public byte[] getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "WALEntry{" + "sipWALEntry=" + sipWALEntry + ", payload=" + payload + '}';
    }

}
