package com.jivesoftware.os.jive.utils.permit;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.jivesoftware.os.jive.utils.base.interfaces.CallbackStream;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.ColumnValueAndTimestamp;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.NeverAcceptsFailureSetOfSortedMaps;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.TenantIdAndRow;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.TenantKeyedColumnValueCallbackStream;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.Timestamper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Permit provider that stores the state of issued permits in HBase.
 *
 * @param <T>
 */
public final class PermitProviderImpl<T> implements PermitProvider {

    private final T tenantId;

    private final int pool;
    private final int minId;
    private final int countIds;
    private final long expires;

    private final String ownerId;

    RowColumnValueStore<T, PermitRowKey, String, Permit, RuntimeException> permitStore;
    Timestamper timestamper;

    // Column qualifier doesn't matter, but we need to use a non-null key for testing with RowColumnValueStoreImpl
    static final String NULL_KEY = "";

    public PermitProviderImpl(
            T tenantId, int pool, int minId, int countIds, long expires,
            String ownerId,
            RowColumnValueStore<T, PermitRowKey, String, Permit, ? extends Exception> permitStore,
            Timestamper timestamper
    ) throws IOException {
        Preconditions.checkArgument(countIds > 0, "Permit pool must have at least one available permit.");
        Preconditions.checkArgument(expires > 0, "A permit must expire in the future.");

        this.tenantId = tenantId;
        this.pool = pool;
        this.minId = minId;
        this.countIds = countIds;
        this.expires = expires;
        this.ownerId = ownerId;
        this.permitStore = new NeverAcceptsFailureSetOfSortedMaps<>(permitStore); // TODO push NeverAcceptsFailureSetOfSortedMaps up to caller
        this.timestamper = timestamper;
    }

    @Override
    public Optional<Permit> requestPermit() {
        long now = timestamper.get();

        PermitIdGenerator permitIdGenerator = new PermitIdGenerator(minId, countIds);
        Optional<Permit> permit = claimExpiredPermit(now, permitIdGenerator);
        if (!permit.isPresent()) {
            permit = claimAvailablePermit(now, permitIdGenerator);
        }

        return permit;
    }

    private Optional<Permit> claimExpiredPermit(long now, PermitIdGenerator permitIdGenerator) {
        List<Permit> issuedPermits = queryIssuedPermits();

        for (Permit issuedPermit : issuedPermits) {
            if (isExpired(issuedPermit.issued, now)) {
                Permit newPermit = new Permit(issuedPermit.pool, issuedPermit.id, now, issuedPermit.owner);
                Optional<Permit> permit = attemptToIssue(new PermitRowKey(issuedPermit.pool, issuedPermit.id), issuedPermit, newPermit);
                if (permit.isPresent()) {
                    return permit;
                }
            }

            permitIdGenerator.markCurrent(issuedPermit.id);
        }

        return Optional.absent();
    }

    private Optional<Permit> claimAvailablePermit(long now, PermitIdGenerator permitIdGenerator) {
        for (int id : permitIdGenerator.listAvailablePermitIds()) {
            Permit newPermit = new Permit(pool, id, now, ownerId);
            Optional<Permit> permit = attemptToIssue(new PermitRowKey(pool, id), null, newPermit);
            if (permit.isPresent()) {
                return permit;
            }
        }

        return Optional.absent();
    }

    @Override
    public Optional<Permit> renewPermit(Permit old) {
        long now = timestamper.get();
        if (!isExpired(old.issued, now)) {
            Permit renewedPermit = new Permit(old.pool, old.id, now, old.owner);
            Optional<Permit> permit = attemptToIssue(new PermitRowKey(old.pool, old.id), old, renewedPermit);
            if (permit.isPresent()) {
                return permit;
            }
        }

        return Optional.absent();
    }

    @Override
    public void releasePermit(Permit permit) {
        long now = timestamper.get();
        if (!isExpired(permit.issued, now)) {
            permitStore.replaceIfEqualToExpected(tenantId, new PermitRowKey(permit.pool, permit.id), NULL_KEY, null, permit, null, null);
        }
    }

    @Override
    public int getNumberOfActivePermitHolders() {
        List<Permit> issuedPermits = queryIssuedPermits();
        Set<String> distinctOwners = new HashSet<>();
        for (Permit permit : issuedPermits) {
            long now = timestamper.get();
            if (!isExpired(permit.issued, now)) {
                distinctOwners.add(permit.owner);
            }
        }
        return distinctOwners.size();
    }

    @Override
    public int getTotalNumberOfConcurrentPermits() {
        return countIds;
    }

    @Override
    public boolean isPermitStillValid(Permit permit) {
        long now = timestamper.get();
        return !isExpired(permit.issued, now);
    }

    private boolean isExpired(long issuedTimestamp, long now) {
        return issuedTimestamp <= now - expires;
    }

    private Optional<Permit> attemptToIssue(PermitRowKey rowKey, Permit expectedIssued, Permit now) {
        if (permitStore.replaceIfEqualToExpected(tenantId, rowKey, NULL_KEY, now, expectedIssued, null, null)) {
            return Optional.of(now);
        }
        return Optional.absent();
    }

    private List<Permit> queryIssuedPermits() {
        final List<Permit> issuedPermits = new ArrayList<>();

        final List<TenantKeyedColumnValueCallbackStream<T, PermitRowKey, String, Permit, Long>> streams = new ArrayList<>();

        PermitRowKey startRow = new PermitRowKey(pool, Integer.MIN_VALUE);
        PermitRowKey endRow = new PermitRowKey(pool + 1, Integer.MIN_VALUE);
        permitStore.getRowKeys(
                tenantId, startRow, endRow, 1000, null,
                new CallbackStream<TenantIdAndRow<T, PermitRowKey>>() {
                    @Override
                    public TenantIdAndRow<T, PermitRowKey> callback(final TenantIdAndRow<T, PermitRowKey> value) {
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
