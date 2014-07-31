package com.jivesoftware.os.jive.utils.permit;

public class IssuedPermit {
    public final PermitRowKey rowKey;
    public final long issued;

    public IssuedPermit(PermitRowKey rowKey, long issued) {
        this.rowKey = rowKey;
        this.issued = issued;
    }
}
