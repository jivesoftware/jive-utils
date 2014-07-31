package com.jivesoftware.os.jive.utils.permit;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.jivesoftware.os.jive.utils.base.interfaces.CallbackStream;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.*;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.Timestamper;

import java.io.IOException;
import java.util.*;

/**
 * Permit provider that stores the state of issued permits in HBase.
 */
public final class PermitProviderImpl<T> implements PermitProvider {
    private final T tenantId;

    private final int pool;
    private final int minId;
    private final int countIds;
    private final long expires;
    private final String label;

    RowColumnValueStore<T, PermitRowKey, String, String, RuntimeException> permitStore;
    Timestamper timestamper;

    static final String COLUMN_ISSUED = "issued";
    static final String COLUMN_LABEL = "label";

    public PermitProviderImpl(
            T tenantId, int pool, int minId, int countIds, long expires, String label,
            RowColumnValueStore<T, PermitRowKey, String, String, ? extends Exception> permitStore,
            Timestamper timestamper
    ) throws IOException {
        Preconditions.checkArgument(countIds > 0, "Permit pool must have at least one available permit.");
        Preconditions.checkArgument(expires > 0, "A permit must expire in the future.");

        this.tenantId = tenantId;
        this.pool = pool;
        this.minId = minId;
        this.countIds = countIds;
        this.expires = expires;
        this.label = label;
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
        List<PermitEntry> permitEntries = queryIssuedPermits();

        for (PermitEntry permitEntry : permitEntries) {
            if (isExpired(permitEntry.issued, now)) {
                Optional<Permit> permit = attemptToIssue(permitEntry.rowKey, permitEntry.issued, now);
                if (permit.isPresent()) {
                    return permit;
                }
            }

            permitIdGenerator.markCurrent(permitEntry.rowKey.id);
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

    @Override
    public void releasePermit(Permit permit) {
        long now = timestamper.get();
        if (!isExpired(permit.issued, now)) {
            PermitRowKey rowKey = new PermitRowKey(permit.pool, permit.id);
            permitStore.removeIfEqualToExpected(tenantId, rowKey, COLUMN_ISSUED, String.valueOf(permit.issued), null);
        }
    }

    private boolean isExpired(long issuedTimestamp, long now) {
        return issuedTimestamp <= now - expires;
    }

    private Optional<Permit> attemptToIssue(PermitRowKey rowKey, Long expectedIssued, Long now) {
        String expectedIssuedString = expectedIssued != null ? String.valueOf(expectedIssued) : null;
        if (permitStore.replaceIfEqualToExpected(
                tenantId, rowKey, COLUMN_ISSUED, String.valueOf(now), expectedIssuedString, null, null
        )) {
            permitStore.add(tenantId, rowKey, COLUMN_LABEL, label, null, null);
            return Optional.of(new Permit(rowKey.pool, rowKey.id, now));
        }
        return Optional.absent();
    }

    @Override
    public int countUniqueLabels() {
        List<PermitEntry> permitEntries = queryIssuedPermits();

        long now = timestamper.get();
        Set<String> labels = new TreeSet<>();
        for (PermitEntry entry : permitEntries) {
            if (!isExpired(entry.issued, now)) {
                labels.add(entry.label);
            }
        }
        return labels.size();
    }

    private List<PermitEntry> queryIssuedPermits() {
        final Map<PermitRowKey, Long> issues = new HashMap<>();
        final Map<PermitRowKey, String> labels = new HashMap<>();

        final List<TenantKeyedColumnValueCallbackStream<T, PermitRowKey, String, String, String>> streams = new ArrayList<>();

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
                                            new CallbackStream<ColumnValueAndTimestamp<String, String, String>>() {
                                                @Override
                                                public ColumnValueAndTimestamp<String, String, String> callback(
                                                        ColumnValueAndTimestamp<String, String, String> value
                                                ) throws Exception {
                                                    if (value != null) {
                                                        if (value.getColumn().equals(COLUMN_ISSUED)) {
                                                            long issued = Long.valueOf(value.getValue());
                                                            issues.put(row, issued);
                                                        }
                                                        else if (value.getColumn().equals(COLUMN_LABEL)) {
                                                            labels.put(row, value.getValue());
                                                        }
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

        List<PermitEntry> permitEntries = new ArrayList<>();
        for (Map.Entry<PermitRowKey, Long> entry : issues.entrySet()) {
            String label = labels.get(entry.getKey());
            if (label == null) {
                // May be in the process of adding a permit, in which case the label might not exist yet.
                continue;
            }
            permitEntries.add(new PermitEntry(entry.getKey(), entry.getValue(), label));
        }

        return permitEntries;
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
