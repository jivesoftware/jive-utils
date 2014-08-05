package com.jivesoftware.os.jive.utils.permit;

import java.util.Objects;

public class Permit implements Comparable<Permit> {

    public final long issued;
    public final long expires;
    public final int id;
    public final String owner;
    public final String tenantId;
    public final String pool;

    public Permit(long issued, long expires, int id, String owner, String tenantId, String pool) {
        this.issued = issued;
        this.expires = expires;
        this.id = id;
        this.owner = owner;
        this.tenantId = tenantId;
        this.pool = pool;
    }

    @Override
    public String toString() {
        return "Permit{" + "issued=" + issued + ", expires=" + expires + ", id=" + id + ", owner=" + owner + ", tenantId=" + tenantId + ", pool=" + pool + '}';
    }

    @Override
    public int compareTo(Permit o) {
        int c = Integer.compare(id, o.id);
        if (c == 0) {
            c = Long.compare(issued, o.issued);
        }
        if (c == 0) {
            c = Long.compare(expires, o.expires);
        }
        if (c == 0) {
            c = owner.compareTo(o.owner);
        }
        if (c == 0) {
            c = tenantId.compareTo(o.tenantId);
        }
        if (c == 0) {
            c = pool.compareTo(o.pool);
        }
        return c;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (int) (this.issued ^ (this.issued >>> 32));
        hash = 71 * hash + (int) (this.expires ^ (this.expires >>> 32));
        hash = 71 * hash + this.id;
        hash = 71 * hash + Objects.hashCode(this.owner);
        hash = 71 * hash + Objects.hashCode(this.tenantId);
        hash = 71 * hash + Objects.hashCode(this.pool);
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
        if (this.issued != other.issued) {
            return false;
        }
        if (this.expires != other.expires) {
            return false;
        }
        if (this.id != other.id) {
            return false;
        }
        if (!Objects.equals(this.owner, other.owner)) {
            return false;
        }
        if (!Objects.equals(this.tenantId, other.tenantId)) {
            return false;
        }
        if (!Objects.equals(this.pool, other.pool)) {
            return false;
        }
        return true;
    }

}
