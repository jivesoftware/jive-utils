package com.jivesoftware.os.jive.utils.permit;

public class Permit {
    private final int pool;
    private final int id;

    public Permit(int pool, int id) {
        this.pool = pool;
        this.id = id;
    }
}
