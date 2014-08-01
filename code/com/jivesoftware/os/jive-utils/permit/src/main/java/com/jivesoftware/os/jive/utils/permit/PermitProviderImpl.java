package com.jivesoftware.os.jive.utils.permit;

import com.google.common.base.Optional;
import com.jivesoftware.os.jive.utils.base.interfaces.CallbackStream;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.ColumnValueAndTimestamp;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.NeverAcceptsFailureSetOfSortedMaps;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.TenantIdAndRow;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.TenantKeyedColumnValueCallbackStream;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.Timestamper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Permit provider that stores the state of issued permits in HBase.
 *
 */
public final class PermitProviderImpl implements PermitProvider {

    private final String ownerId;

    RowColumnValueStore<String, PermitRowKey, String, Permit, RuntimeException> permitStore;
    Timestamper timestamper;

    // Column qualifier doesn't matter, but we need to use a non-null key for testing with RowColumnValueStoreImpl
    static final String NULL_KEY = "";

    public PermitProviderImpl(
            String ownerId,
            RowColumnValueStore<String, PermitRowKey, String, Permit, ? extends Exception> permitStore,
            Timestamper timestamper
    ) throws IOException {

        this.ownerId = ownerId;
        this.permitStore = new NeverAcceptsFailureSetOfSortedMaps<>(permitStore); // TODO push NeverAcceptsFailureSetOfSortedMaps up to caller
        this.timestamper = timestamper;
    }

    @Override
    public List<Permit> requestPermit(String tenantId, PermitConfig permitConfig, int count) {
        long now = timestamper.get();

        List<Permit> permits = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            PermitIdGenerator permitIdGenerator = new PermitIdGenerator(permitConfig.getMinId(), permitConfig.getCountIds());
            Optional<Permit> permit = claimExpiredPermit(tenantId, permitConfig.getPool(), now, permitIdGenerator);
            if (!permit.isPresent()) {
                permit = claimAvailablePermit(tenantId, permitConfig, now, permitIdGenerator);
            }
            if (permit.isPresent()) {
                permits.add(permit.get());
            }
        }
        return permits;
    }

    private Optional<Permit> claimExpiredPermit(String tenantId, String poolId, long now, PermitIdGenerator permitIdGenerator) {
        List<Permit> issuedPermits = queryIssuedPermits(tenantId, poolId);

        for (Permit issuedPermit : issuedPermits) {
            if (isExpired(issuedPermit, now)) {
                Permit newPermit = new Permit(now, issuedPermit.expires, issuedPermit.id, issuedPermit.owner, issuedPermit.tenantId, issuedPermit.pool);
                Optional<Permit> permit = attemptToIssue(tenantId, new PermitRowKey(issuedPermit.pool, issuedPermit.id), issuedPermit, newPermit);
                if (permit.isPresent()) {
                    return permit;
                }
            }

            permitIdGenerator.markCurrent(issuedPermit.id);
        }

        return Optional.absent();
    }

    private Optional<Permit> claimAvailablePermit(String tenantId, PermitConfig permitConfig, long now, PermitIdGenerator permitIdGenerator) {
        for (int id : permitIdGenerator.listAvailablePermitIds()) {
            Permit newPermit = new Permit(now, permitConfig.getExpires(), id, ownerId, tenantId, permitConfig.getPool());
            Optional<Permit> permit = attemptToIssue(tenantId, new PermitRowKey(permitConfig.getPool(), id), null, newPermit);
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
        for (Permit oldPermit : oldPermits) {
            if (isExpired(oldPermit, now)) {
                Optional<Permit> permit = attemptToIssue(oldPermit.tenantId, new PermitRowKey(oldPermit.pool, oldPermit.id), oldPermit, null);
                renewed.add(permit);
            } else {
                Permit renewedPermit = new Permit(now, oldPermit.expires, oldPermit.id, oldPermit.owner, oldPermit.tenantId, oldPermit.pool);
                Optional<Permit> permit = attemptToIssue(oldPermit.tenantId, new PermitRowKey(oldPermit.pool, oldPermit.id), oldPermit, renewedPermit);
                renewed.add(permit);
            }
        }

        return renewed;
    }

    @Override
    public void releasePermit(Collection<Permit> permits) {
        long now = timestamper.get();
        for (Permit permit : permits) {
            if (!isExpired(permit, now)) {
                permitStore.replaceIfEqualToExpected(permit.tenantId, new PermitRowKey(permit.pool, permit.id), NULL_KEY, null, permit, null, null);
            }
        }
    }

    @Override
    public int getNumberOfActivePermitHolders(String tenantId, PermitConfig permitConfig) {
        List<Permit> issuedPermits = queryIssuedPermits(tenantId, permitConfig.getPool());
        Set<String> distinctOwners = new HashSet<>();
        for (Permit permit : issuedPermits) {
            long now = timestamper.get();
            if (!isExpired(permit, now)) {
                distinctOwners.add(permit.owner);
            }
        }
        return distinctOwners.size();
    }

    @Override
    public boolean isPermitStillValid(Permit permit) {
        long now = timestamper.get();
        return !isExpired(permit, now);
    }

    private boolean isExpired(Permit permit, long now) {
        return permit.issued <= now - permit.expires;
    }

    private Optional<Permit> attemptToIssue(String tenantId, PermitRowKey rowKey, Permit expectedIssued, Permit now) {
        if (permitStore.replaceIfEqualToExpected(tenantId, rowKey, NULL_KEY, now, expectedIssued, null, null)) {
            if (now == null) {
                return Optional.absent();
            } else {
                return Optional.of(now);
            }
        }
        return Optional.absent();
    }

    private List<Permit> queryIssuedPermits(String tenantId, String poolId) {
        final List<Permit> issuedPermits = new ArrayList<>();

        final List<TenantKeyedColumnValueCallbackStream<String, PermitRowKey, String, Permit, Long>> streams = new ArrayList<>();

        PermitRowKey startRow = new PermitRowKey(poolId, Integer.MIN_VALUE);
        PermitRowKey endRow = new PermitRowKey(poolId, Integer.MAX_VALUE);
        permitStore.getRowKeys(
                tenantId, startRow, endRow, 1000, null,
                new CallbackStream<TenantIdAndRow<String, PermitRowKey>>() {
                    @Override
                    public TenantIdAndRow<String, PermitRowKey> callback(final TenantIdAndRow<String, PermitRowKey> value) {
                        if (value != null) {
                            final PermitRowKey row = value.getRow();
                            streams.add(
                                    new TenantKeyedColumnValueCallbackStream<>(
                                            value.getTenantId(), row,
                                            new CallbackStream<ColumnValueAndTimestamp<String, Permit, Long>>() {
                                                @Override
                                                public ColumnValueAndTimestamp<String, Permit, Long> callback(
                                                        ColumnValueAndTimestamp<String, Permit, Long> value
                                                ) throws Exception {
                                                    if (value != null) {
                                                        issuedPermits.add(value.getValue());
                                                    }
                                                    return value;
                                                }
                                            }
                                    )
                            );
                        }
                        return value;
                    }
                }
        );

        permitStore.multiRowGetAll(streams);

        return issuedPermits;
    }

}
