package com.jivesoftware.os.jive.utils.permit;

public class Permit {
    public final int pool;
    public final int id;
    public final long issued;

    public Permit(int pool, int id, long issued) {
        this.pool = pool;
        this.id = id;
        this.issued = issued;
    }
}
