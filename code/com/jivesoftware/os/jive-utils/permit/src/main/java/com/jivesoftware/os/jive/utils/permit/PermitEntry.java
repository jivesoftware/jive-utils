package com.jivesoftware.os.jive.utils.permit;

public class PermitEntry {
    public final PermitRowKey rowKey;
    public final long issued;
    public final String label;

    public PermitEntry(PermitRowKey rowKey, long issued, String label) {
        this.rowKey = rowKey;
        this.issued = issued;
        this.label = label;
    }
}
