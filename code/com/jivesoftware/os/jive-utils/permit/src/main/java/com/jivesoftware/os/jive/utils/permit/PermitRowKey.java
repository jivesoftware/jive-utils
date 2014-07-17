package com.jivesoftware.os.jive.utils.permit;

public class PermitRowKey {
    public final int pool;
    public final int id;

    public PermitRowKey(int pool, int id) {
        this.pool = pool;
        this.id = id;
    }
}
