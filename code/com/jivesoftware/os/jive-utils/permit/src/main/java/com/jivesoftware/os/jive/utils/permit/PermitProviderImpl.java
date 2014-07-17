package com.jivesoftware.os.jive.utils.permit;

import com.jivesoftware.os.jive.utils.base.interfaces.CallbackStream;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.ColumnValueAndTimestamp;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.NeverAcceptsFailureSetOfSortedMaps;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.SetOfSortedMapsImplInitializer;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.TenantIdAndRow;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.TenantKeyedColumnValueCallbackStream;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.CurrentTimestamper;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.api.TypeMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.primatives.LongTypeMarshaller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Permit provider that stores the state of issued permits in HBase.
 */
public final class PermitProviderImpl<T> implements PermitProvider {
    private final T tenantId;

    private final int pool;
    private final int minId;
    private final int countIds;
    private final long expires;

    NeverAcceptsFailureSetOfSortedMaps<T, PermitRowKey, Void, Long> permitStore;

    public PermitProviderImpl(
            T tenantId, int pool, int minId, int countIds, long expires, String tableNameSpace,
            TypeMarshaller<T> tenantIdMarshaller,
            SetOfSortedMapsImplInitializer<? extends Exception> setOfSortedMapsImplInitializer
    ) throws IOException {
        this.tenantId = tenantId;
        this.pool = pool;
        this.minId = minId;
        this.countIds = countIds;
        this.expires = expires;

        permitStore = new NeverAcceptsFailureSetOfSortedMaps<>(
                setOfSortedMapsImplInitializer.initialize(
                        tableNameSpace,
                        "permit.log",
                        "p",
                        new TenantLengthAndTenantFirstRowColumnValueStoreMarshaller<>(
                                tenantIdMarshaller,
                                new PermitRowKeyMarshaller(),
                                new VoidTypeMarshaller(),
                                new LongTypeMarshaller()
                        ),
                        new CurrentTimestamper()
                )
        );
    }

    @Override
    public Permit requestPermit() throws OutOfPermitsException {
        long now = System.currentTimeMillis();

        Set<Integer> takenPermits = new TreeSet<>();
        Permit permit = claimExpiredPermit(now, takenPermits);
        if (permit == null) {
            permit = claimAvailablePermit(now, takenPermits);
        }

        if (permit == null) {
            throw new OutOfPermitsException();
        }

        return permit;
    }

    private Permit claimExpiredPermit(long now, Set<Integer> takenPermits) {
        List<IssuedPermit> issuedPermits = getIssuedPermits();

        for (IssuedPermit permit : issuedPermits) {
            if (permit.issued < now - expires) {
                if (attemptToIssue(permit.rowKey, permit.issued, now)) {
                    return new Permit(permit.rowKey.pool, permit.rowKey.id);
                }
            }

            takenPermits.add(permit.rowKey.id);
        }

        return null;
    }

    private Permit claimAvailablePermit(long now, Set<Integer> takenPermits) {
        Set<Integer> availablePermits = new TreeSet<>();
        for (int i = minId; i < minId + countIds; i++) {
            availablePermits.add(i);
        }
        availablePermits.removeAll(takenPermits);

        for (int permit : availablePermits) {
            if (attemptToIssue(new PermitRowKey(pool, permit), null, now)) {
                return new Permit(pool, permit);
            }
        }

        return null;
    }

    private boolean attemptToIssue(PermitRowKey rowKey, Long expectedIssued, long now) {
        return permitStore.replaceIfEqualToExpected(tenantId, rowKey, null, now, expectedIssued, null, null);
    }

    private List<IssuedPermit> getIssuedPermits() {
        final List<IssuedPermit> issuedPermits = new ArrayList<>();

        final List<TenantKeyedColumnValueCallbackStream<T, PermitRowKey, Void, Long, Long>> streams = new ArrayList<>();

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
                                            new CallbackStream<ColumnValueAndTimestamp<Void, Long, Long>>() {
                                                @Override
                                                public ColumnValueAndTimestamp<Void, Long, Long> callback(
                                                        ColumnValueAndTimestamp<Void, Long, Long> value
                                                ) throws Exception {
                                                    issuedPermits.add(new IssuedPermit(row, value.getValue()));
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
