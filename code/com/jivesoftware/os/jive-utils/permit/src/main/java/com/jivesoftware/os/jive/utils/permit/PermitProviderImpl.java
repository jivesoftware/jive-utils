package com.jivesoftware.os.jive.utils.permit;

import com.google.common.base.Preconditions;
import com.jivesoftware.os.jive.utils.base.interfaces.CallbackStream;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.ColumnValueAndTimestamp;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.NeverAcceptsFailureSetOfSortedMaps;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.SetOfSortedMapsImplInitializer;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.TenantIdAndRow;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.TenantKeyedColumnValueCallbackStream;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.TenantLengthAndTenantFirstRowColumnValueStoreMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.CurrentTimestamper;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.api.TypeMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.primatives.LongTypeMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.primatives.VoidTypeMarshaller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

    RowColumnValueStore<T, PermitRowKey, Void, Long, RuntimeException> permitStore;

    public PermitProviderImpl(
            T tenantId, int pool, int minId, int countIds, long expires, String tableNameSpace,
            TypeMarshaller<T> tenantIdMarshaller,
            SetOfSortedMapsImplInitializer<? extends Exception> setOfSortedMapsImplInitializer
    ) throws IOException {
        Preconditions.checkArgument(countIds > 0, "Permit pool must have at least one available permit.");
        Preconditions.checkArgument(expires > 0, "A permit must expire in the future.");

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

        PermitIdGenerator permitIdGenerator = new PermitIdGenerator(minId, countIds);
        Permit permit = claimExpiredPermit(now, permitIdGenerator);
        if (permit == null) {
            permit = claimAvailablePermit(now, permitIdGenerator);
        }

        if (permit == null) {
            throw new OutOfPermitsException();
        }

        return permit;
    }

    private Permit claimExpiredPermit(long now, PermitIdGenerator permitIdGenerator) {
        List<IssuedPermit> issuedPermits = queryIssuedPermits();

        for (IssuedPermit permit : issuedPermits) {
            if (permit.issued < now - expires) {
                if (attemptToIssue(permit.rowKey, permit.issued, now)) {
                    return new Permit(permit.rowKey.pool, permit.rowKey.id);
                }
            }

            permitIdGenerator.markCurrent(permit.rowKey.id);
        }

        return null;
    }

    private Permit claimAvailablePermit(long now, PermitIdGenerator permitIdGenerator) {
        for (int permit : permitIdGenerator.listAvailablePermitIds()) {
            if (attemptToIssue(new PermitRowKey(pool, permit), null, now)) {
                return new Permit(pool, permit);
            }
        }

        return null;
    }

    private boolean attemptToIssue(PermitRowKey rowKey, Long expectedIssued, long now) {
        return permitStore.replaceIfEqualToExpected(tenantId, rowKey, null, now, expectedIssued, null, null);
    }

    private List<IssuedPermit> queryIssuedPermits() {
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

    static class PermitIdGenerator {
        private final int minId;
        private final int countIds;
        private final Set<Integer> currentPermits = new TreeSet<>();

        private PermitIdGenerator(int minId, int countIds) {
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
