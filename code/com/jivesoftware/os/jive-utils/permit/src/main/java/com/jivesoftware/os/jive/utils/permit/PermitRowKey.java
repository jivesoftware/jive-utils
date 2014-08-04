package com.jivesoftware.os.jive.utils.permit;

import java.util.Objects;

public class PermitRowKey implements Comparable<PermitRowKey> {

    public final String pool;
    public final int id;

    public PermitRowKey(String pool, int id) {
        this.pool = pool;
        this.id = id;
    }

    @Override
    public String toString() {
        return "PermitRowKey{" + "pool=" + pool + ", id=" + id + '}';
    }


    @Override
    public int compareTo(PermitRowKey o) {
        int i = pool.compareTo(o.pool);
        if (i == 0) {
            i = Integer.compare(id, o.id);
        }
        return i;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.pool);
        hash = 83 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PermitRowKey other = (PermitRowKey) obj;
        if (!Objects.equals(this.pool, other.pool)) {
            return false;
        }
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

}
