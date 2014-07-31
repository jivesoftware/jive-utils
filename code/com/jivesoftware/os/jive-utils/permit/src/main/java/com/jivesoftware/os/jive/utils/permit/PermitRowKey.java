package com.jivesoftware.os.jive.utils.permit;

public class PermitRowKey implements Comparable<PermitRowKey> {
    public final int pool;
    public final int id;

    public PermitRowKey(int pool, int id) {
        this.pool = pool;
        this.id = id;
    }

    @Override
    public int compareTo(PermitRowKey o) {
        if (pool != o.pool) {
            return pool > o.pool ? 1 : -1;
        }

        if (id != o.id) {
            return id > o.id ? 1 : -1;
        }

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PermitRowKey that = (PermitRowKey) o;

        if (id != that.id) {
            return false;
        }
        if (pool != that.pool) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = pool;
        result = 31 * result + id;
        return result;
    }
}
