package com.jivesoftware.os.jive.utils.hwal.shared.hbase;

import com.jivesoftware.os.jive.utils.id.TenantId;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.MultiAdd;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumValueTimestampAdd;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.ConstantTimestamper;
import java.util.List;

/**
 * @author jonathan
 */
public class WALWriterImpl<C> implements WALWriter {

    public static interface CollisionId {

        long collisionId(WALEntry activity);
    }

    public static interface ColumnKey<C> {

        C provideColumnKey(WALEntry activity, CollisionId collisionId);
    }

    private final RowColumnValueStore<TenantId, WALRow, C, WALEntry, ? extends Exception> wal;
    private final ColumnKey<C> columnKey;
    private final CollisionId collisionId;

    public WALWriterImpl(
        RowColumnValueStore<TenantId, WALRow, C, WALEntry, ? extends Exception> wal,
        ColumnKey<C> columnKey,
        CollisionId collisionId) {
        this.wal = wal;
        this.columnKey = columnKey;
        this.collisionId = collisionId;
    }

    @Override
    public void write(TenantId tenantId, List<WALEntry> entries) throws Exception {
        MultiAdd<WALRow, C, WALEntry> rawAdds = new MultiAdd<>();
        for (WALEntry entry : entries) {
            rawAdds.add(
                new WALRow(entry.getTopicId()),
                columnKey.provideColumnKey(entry, collisionId),
                entry,
                new ConstantTimestamper(entry.timestamp)
            );
        }

        List<RowColumValueTimestampAdd<WALRow, C, WALEntry>> took = rawAdds.take();
        wal.multiRowsMultiAdd(tenantId, took);
    }

}
