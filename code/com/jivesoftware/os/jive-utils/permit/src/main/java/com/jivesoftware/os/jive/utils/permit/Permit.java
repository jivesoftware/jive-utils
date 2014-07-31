package com.jivesoftware.os.jive.utils.permit;

import java.util.Objects;

public class Permit {
    public final int pool;
    public final int id;
    public final long issued;
    public final String owner;

    public Permit(int pool, int id, long issued, String owner) {
        this.pool = pool;
        this.id = id;
        this.issued = issued;
        this.owner = owner;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + this.pool;
        hash = 59 * hash + this.id;
        hash = 59 * hash + (int) (this.issued ^ (this.issued >>> 32));
        hash = 59 * hash + Objects.hashCode(this.owner);
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
        final Permit other = (Permit) obj;
        if (this.pool != other.pool) {
            return false;
        }
        if (this.id != other.id) {
            return false;
        }
        if (this.issued != other.issued) {
            return false;
        }
        if (!Objects.equals(this.owner, other.owner)) {
            return false;
        }
        return true;
    }


}
