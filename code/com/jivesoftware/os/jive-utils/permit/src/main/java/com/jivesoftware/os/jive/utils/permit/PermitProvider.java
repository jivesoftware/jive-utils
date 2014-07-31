package com.jivesoftware.os.jive.utils.permit;

import com.google.common.base.Optional;

public interface PermitProvider {

    /**
     * @return a new permit from the pool of expired or not-yet-issued permits or an Optional.absent() is all permits are taken.
     */
    Optional<Permit> requestPermit();

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
     * Releases the provided permit if it is still valid
     *
     * @param permit
     */
    void releasePermit(Permit permit);

    /**
     * Returns the number of distinct permit holders
     *
     * @return
     */
    int getNumberOfActivePermitHolders();

    /**
     * The the total number of permits that can concurrently be handed out by this permit provider.
     *
     * @return
     */
    int getTotalNumberOfConcurrentPermits();

    /**
     *
     * @param permit
     * @return
     */
    boolean isPermitStillValid(Permit permit);
}
