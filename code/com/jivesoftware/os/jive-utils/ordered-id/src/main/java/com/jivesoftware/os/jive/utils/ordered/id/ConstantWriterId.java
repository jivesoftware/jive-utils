package com.jivesoftware.os.jive.utils.ordered.id;

public class ConstantWriterId implements WriterId {
    private final int id;

    public ConstantWriterId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
