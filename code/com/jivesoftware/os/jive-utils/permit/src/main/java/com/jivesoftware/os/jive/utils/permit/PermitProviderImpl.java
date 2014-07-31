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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Permit provider that stores the state of issued permits in HBase.
 * @param <T>
 */
public final class PermitProviderImpl<T> implements PermitProvider {
    private final T tenantId;

    private final int pool;
    private final int minId;
    private final int countIds;
    private final long expires;

    RowColumnValueStore<T, PermitRowKey, String, Long, RuntimeException> permitStore;
    Timestamper timestamper;

    // Column qualifier doesn't matter, but we need to use a non-null key for testing with RowColumnValueStoreImpl
    static final String NULL_KEY = "";

    public PermitProviderImpl(
            T tenantId, int pool, int minId, int countIds, long expires,
            RowColumnValueStore<T, PermitRowKey, String, Long, ? extends Exception> permitStore,
            Timestamper timestamper
    ) throws IOException {
        Preconditions.checkArgument(countIds > 0, "Permit pool must have at least one available permit.");
        Preconditions.checkArgument(expires > 0, "A permit must expire in the future.");

        this.tenantId = tenantId;
        this.pool = pool;
        this.minId = minId;
        this.countIds = countIds;
        this.expires = expires;
        this.permitStore = new NeverAcceptsFailureSetOfSortedMaps<>(permitStore);
        this.timestamper = timestamper;
    }

    @Override
    public Permit requestPermit() throws OutOfPermitsException {
        long now = timestamper.get();

        PermitIdGenerator permitIdGenerator = new PermitIdGenerator(minId, countIds);
        Optional<Permit> permit = claimExpiredPermit(now, permitIdGenerator);
        if (!permit.isPresent()) {
            permit = claimAvailablePermit(now, permitIdGenerator);
        }

        if (!permit.isPresent()) {
            throw new OutOfPermitsException();
        }

        return permit.get();
    }

    private Optional<Permit> claimExpiredPermit(long now, PermitIdGenerator permitIdGenerator) {
        List<IssuedPermit> issuedPermits = queryIssuedPermits();

        for (IssuedPermit issuedPermit : issuedPermits) {
            if (isExpired(issuedPermit.issued, now)) {
                Optional<Permit> permit = attemptToIssue(issuedPermit.rowKey, issuedPermit.issued, now);
                if (permit.isPresent()) {
                    return permit;
                }
            }

            permitIdGenerator.markCurrent(issuedPermit.rowKey.id);
        }

        return Optional.absent();
    }

    private Optional<Permit> claimAvailablePermit(long now, PermitIdGenerator permitIdGenerator) {
        for (int id : permitIdGenerator.listAvailablePermitIds()) {
            Optional<Permit> permit = attemptToIssue(new PermitRowKey(pool, id), null, now);
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
            Optional<Permit> permit = attemptToIssue(new PermitRowKey(old.pool, old.id), old.issued, now);
            if (permit.isPresent()) {
                return permit;
            }
        }

        return Optional.absent();
    }

    private boolean isExpired(long issuedTimestamp, long now) {
        return issuedTimestamp <= now - expires;
    }

    private Optional<Permit> attemptToIssue(PermitRowKey rowKey, Long expectedIssued, Long now) {
        if (permitStore.replaceIfEqualToExpected(tenantId, rowKey, NULL_KEY, now, expectedIssued, null, null)) {
            return Optional.of(new Permit(rowKey.pool, rowKey.id, now));
        }
        return Optional.absent();
    }

    private List<IssuedPermit> queryIssuedPermits() {
        final List<IssuedPermit> issuedPermits = new ArrayList<>();

        final List<TenantKeyedColumnValueCallbackStream<T, PermitRowKey, String, Long, String>> streams = new ArrayList<>();

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
                                            new CallbackStream<ColumnValueAndTimestamp<String, Long, String>>() {
                                                @Override
                                                public ColumnValueAndTimestamp<String, Long, String> callback(
                                                        ColumnValueAndTimestamp<String, Long, String> value
                                                ) throws Exception {
                                                    if (value != null) {
                                                        issuedPermits.add(new IssuedPermit(row, value.getValue()));
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

    static class PermitIdGenerator {
        private final int minId;
        private final int countIds;
        private final Set<Integer> currentPermits = new TreeSet<>();

        PermitIdGenerator(int minId, int countIds) {
            this.minId = minId;
            this.countIds = countIds;
        }

        public void markCurrent(int id) {
            currentPermits.add(id);
        }

        public List<Integer> listAvailablePermitIds() {
            List<Integer> availablePermits = new ArrayList<>();
            for (int i = minId; i < minId + countIds; i++) {
                availablePermits.add(i);
            }
            availablePermits.removeAll(currentPermits);
            Collections.shuffle(availablePermits);
            return availablePermits;
        }
    }
}
