package com.jivesoftware.os.jive.utils.ordered.id;

public class ExpiringWriterId implements WriterId {
    private final int id;
    private final long expires;

    public ExpiringWriterId(int id, long expires) {
        this.id = id;
        this.expires = expires;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean isValid() {
        return expires > System.currentTimeMillis();
    }
}
