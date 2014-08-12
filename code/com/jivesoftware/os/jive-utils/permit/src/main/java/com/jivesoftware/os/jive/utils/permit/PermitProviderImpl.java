package com.jivesoftware.os.jive.utils.permit;

import com.google.common.base.Optional;
import com.jivesoftware.os.jive.utils.base.interfaces.CallbackStream;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.NeverAcceptsFailureSetOfSortedMaps;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.Timestamper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Permit provider that stores the state of issued permits in HBase.
 *
 */
public final class PermitProviderImpl implements PermitProvider {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();

    private final String ownerId;

    RowColumnValueStore<String, String, Integer, Permit, RuntimeException> permitStore;
    Timestamper timestamper;

    // Column qualifier doesn't matter, but we need to use a non-null key for testing with RowColumnValueStoreImpl
    static final String NULL_KEY = "";

    public PermitProviderImpl(
            String ownerId,
            RowColumnValueStore<String, String, Integer, Permit, ? extends Exception> permitStore,
            Timestamper timestamper
    ) throws IOException {

        this.ownerId = ownerId;
        this.permitStore = new NeverAcceptsFailureSetOfSortedMaps<>(permitStore); // TODO push NeverAcceptsFailureSetOfSortedMaps up to caller
        this.timestamper = timestamper;
    }

    @Override
    public List<Permit> requestPermit(String tenantId, String permitGroup, PermitConfig permitConfig, int count) {
        long now = timestamper.get();

        List<Permit> permits = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            PermitIdGenerator permitIdGenerator = new PermitIdGenerator(permitConfig.getMinId(), permitConfig.getCountIds());
            Optional<Permit> permit = claimExpiredPermit(tenantId, permitGroup, now, permitIdGenerator);
            if (!permit.isPresent()) {
                permit = claimAvailablePermit(tenantId, permitGroup, permitConfig, now, permitIdGenerator);
            }
            if (permit.isPresent()) {
                permits.add(permit.get());
            }
        }
        return permits;
    }

    private Optional<Permit> claimExpiredPermit(String tenantId, String permitGroup, long now, PermitIdGenerator permitIdGenerator) {
        List<Permit> issuedPermits = queryIssuedPermits(tenantId, permitGroup);

        for (Permit issuedPermit : issuedPermits) {
            if (isExpired(issuedPermit, now)) {
                Permit newPermit = new Permit(now,
                        issuedPermit.expiresInNMillis, issuedPermit.id, issuedPermit.owner, issuedPermit.tenantId, issuedPermit.pool);
                Optional<Permit> permit = attemptToIssue(tenantId, issuedPermit.pool, issuedPermit.id, issuedPermit, newPermit);
                if (permit.isPresent()) {
                    return permit;
                }
            }

            permitIdGenerator.markCurrent(issuedPermit.id);
        }

        return Optional.absent();
    }

    private Optional<Permit> claimAvailablePermit(String tenantId,
            String permitGroup,
            PermitConfig permitConfig,
            long now,
            PermitIdGenerator permitIdGenerator) {

        for (int id : permitIdGenerator.listAvailablePermitIds()) {
            Permit newPermit = new Permit(now, permitConfig.getExpires(), id, ownerId, tenantId, permitGroup);
            Optional<Permit> permit = attemptToIssue(tenantId, permitGroup, id, null, newPermit);
            if (permit.isPresent()) {
                return permit;
            }
        }

        return Optional.absent();
    }

    @Override
    public List<Optional<Permit>> renewPermit(List<Permit> oldPermits) {
        long now = timestamper.get();
        List<Optional<Permit>> renewed = new ArrayList<>();
        List<Permit> expired = new ArrayList<>();
        for (Permit oldPermit : oldPermits) {
            if (isExpired(oldPermit, now)) {
                expired.add(oldPermit);
                renewed.add(Optional.<Permit>absent());
            } else {
                Permit renewedPermit = new Permit(now, oldPermit.expiresInNMillis, oldPermit.id, oldPermit.owner, oldPermit.tenantId, oldPermit.pool);
                Optional<Permit> permit = attemptToIssue(oldPermit.tenantId, oldPermit.pool, oldPermit.id, oldPermit, renewedPermit);
                renewed.add(permit);
            }
        }
        releasePermit(expired);
        return renewed;
    }

    @Override
    public void releasePermit(Collection<Permit> permits) {
        long now = timestamper.get();
        for (Permit permit : permits) {
            if (!isExpired(permit, now)) {
                permitStore.replaceIfEqualToExpected(permit.tenantId, permit.pool, permit.id, null, permit, null, timestamper);
            }
        }
    }

    @Override
    public List<Permit> getAllIssuedPermits(String tenantId, String permitGroup, PermitConfig permitConfig) {
        return queryIssuedPermits(tenantId, permitGroup);
    }

    @Override
    public Optional<Permit> isExpired(Permit permit) {
        if (permit == null) {
            return Optional.absent();
        }
        long age = (timestamper.get() - permit.issuedAtTimeInMillis);
        if (age < permit.expiresInNMillis) {
            if (age > permit.expiresInNMillis / 2) { // TODO expose to config?
                return renewPermit(Arrays.asList(permit)).get(0);
            } else {
                return Optional.of(permit);
            }
        } else {
            return Optional.absent();
        }
    }

    private boolean isExpired(Permit permit, long now) {
        if (permit == null) {
            return true;
        }
        return permit.issuedAtTimeInMillis <= now - permit.expiresInNMillis;
    }

    private Optional<Permit> attemptToIssue(String tenantId, String pool, int it, Permit expectedIssued, Permit now) {
        if (permitStore.replaceIfEqualToExpected(tenantId, pool, it, now, expectedIssued, null, timestamper)) {
            if (now == null) {
                return Optional.absent();
            } else {
                return Optional.of(now);
            }
        }
        return Optional.absent();
    }

    private List<Permit> queryIssuedPermits(String tenantId, final String groupId) {
        final List<Permit> issuedPermits = new ArrayList<>();

        permitStore.getValues(tenantId, groupId, null, Long.MAX_VALUE, 10000, false, null, null, new CallbackStream<Permit>() {

            @Override
            public Permit callback(Permit value) throws Exception {
                if (value != null) {
                    issuedPermits.add(value);
                }
                return value;
            }
        });
        return issuedPermits;
    }

}
