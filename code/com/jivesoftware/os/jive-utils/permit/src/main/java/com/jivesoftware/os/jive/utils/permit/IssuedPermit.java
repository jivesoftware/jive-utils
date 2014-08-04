package com.jivesoftware.os.jive.utils.permit;

class IssuedPermit {
    public final PermitRowKey rowKey;
    public final long issued;

    IssuedPermit(PermitRowKey rowKey, long issued) {
        this.rowKey = rowKey;
        this.issued = issued;
    }
}
