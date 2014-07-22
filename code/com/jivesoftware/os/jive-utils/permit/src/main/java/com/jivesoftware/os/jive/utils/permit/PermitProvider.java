package com.jivesoftware.os.jive.utils.permit;

import com.google.common.base.Optional;

public interface PermitProvider<T> {
    /**
     * @return a new permit from the pool of expired or not-yet-issued permits.
     */
    Permit requestPermit() throws OutOfPermitsException;

    /**
     * Attempts to renew a permit that is not yet expired.
     *
     * Once a permit is renewed, the old permit should no longer be used.
     *
     * @param old the existing permit that needs to be renewed
     * @return a Permit, or nothing in the case that the Permit was already expired
     */
    Optional<Permit> renewPermit(Permit old);
}
