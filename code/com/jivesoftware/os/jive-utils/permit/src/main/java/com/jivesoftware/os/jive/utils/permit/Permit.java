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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Permit permit = (Permit) o;

        if (id != permit.id) {
            return false;
        }
        if (issued != permit.issued) {
            return false;
        }
        if (pool != permit.pool) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = pool;
        result = 31 * result + id;
        result = 31 * result + (int) (issued ^ (issued >>> 32));
        return result;
    }
}
