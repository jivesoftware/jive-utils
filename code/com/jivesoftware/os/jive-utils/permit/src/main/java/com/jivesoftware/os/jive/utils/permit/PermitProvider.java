package com.jivesoftware.os.jive.utils.permit;

import com.google.common.base.Optional;

public interface PermitProvider {
    /**
     * @return a new permit from the pool of expired or not-yet-issued permits.
     */
    Permit requestPermit() throws OutOfPermitsException;

    /**
     * Attempts to renew a permit.
     *
     * The original permit should not be used after calling this method.
     *
     * @param old the existing permit that needs to be renewed
     * @return a renewed Permit, or nothing in the case that the Permit was already expired
     */
    Optional<Permit> renewPermit(Permit old);

    /**
     * @return the number of unique labels attached to current permits in the pool
     */
    int countUniqueLabels();
}
