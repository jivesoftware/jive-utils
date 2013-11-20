/*
 * Copyright 2013 Jive Software, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.jivesoftware.os.jive.utils.base.service.pojo;

/**
 * Used to partition one customers data from another. This is also used to control routing and load balance. The timestamp allows us to understand latency. It
 * provides us with a point in time that we can us when looking at logs.
 *
 * @author jonathan
 * @author scott
 */
public final class Tenancy {

    public static final long SYSTEM_ACTOR_ID = -1l;
    private final String tenantId;
    private final long actorId;
    private final long timestampInMillis;

    /**
     *
     * @param tenantId this is typically the string representation of a type1 uuid.
     * @param actorId 0 is reserved as null. negative numbers are process level actors. positive numbers are human actors.
     * @param timestampInMillis java utc epoch millis
     */
    public Tenancy(String tenantId, long actorId, long timestampInMillis) {
        this.tenantId = tenantId;
        this.actorId = actorId;
        this.timestampInMillis = timestampInMillis;
    }

    public final String getTenantId() {
        return tenantId;
    }

    public final long getActorId() {
        return actorId;
    }

    public final long getTimestampInMillis() {
        return timestampInMillis;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tenancy)) {
            return false;
        }

        Tenancy tenancy = (Tenancy) o;

        if (actorId != tenancy.actorId) {
            return false;
        }
        if (timestampInMillis != tenancy.timestampInMillis) {
            return false;
        }
        if (tenantId != null ? !tenantId.equals(tenancy.tenantId) : tenancy.tenantId != null) {
            return false;
        }

        return true;
    }

    @Override
    public final int hashCode() {
        int result = tenantId != null ? tenantId.hashCode() : 0;
        result = 31 * result + (int) (actorId ^ (actorId >>> 32));
        result = 31 * result + (int) (timestampInMillis ^ (timestampInMillis >>> 32));
        return result;
    }

    @Override
    public final String toString() {
        return "Tenancy{"
                + "tenantId='" + tenantId + '\''
                + ", actorId=" + actorId
                + ", timestampInMillis=" + timestampInMillis
                + '}';
    }
}
