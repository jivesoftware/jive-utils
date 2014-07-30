package com.jivesoftware.os.jive.utils.hwal.shared.hbase;

import com.jivesoftware.jive.platform.miru.miru.api.activity.MiruPartitionedActivity;
import com.jivesoftware.os.jive.utils.hwal.shared.hbase.WALWriterImpl.CollisionId;
import com.jivesoftware.os.jive.utils.hwal.shared.hbase.WALWriterImpl.ColumnKey;
import com.jivesoftware.os.jive.utils.id.TenantId;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;
import java.util.List;

/** @author jonathan */
public class WriteToWAL implements WALWriter {
    private final WALWriter walWriter;
    private final WALWriter sipWALWriter;

    public WriteToWAL(
        RowColumnValueStore<TenantId, WALRow, WALColumnKey, WALEntry, ? extends Exception> wal,
        RowColumnValueStore<TenantId, WALRow, SipWALColumnKey, WALEntry, ? extends Exception> sipWAL) {

        this.walWriter = new WALWriterImpl<WALColumnKey>(wal,
            ACTIVITY_WAL_COLUMN_KEY,
            ACTIVITY_WAL_COLLISIONID);

        this.sipWALWriter = new WALWriterImpl<SipWALColumnKey>(sipWAL,
            ACTIVITY_SIP_WAL_COLUMN_KEY,
            ACTIVITY_SIP_WAL_COLLISIONID);
    }

    @Override
    public void write(TenantId tenantId, List<WALEntry> entries) throws Exception {
        walWriter.write(tenantId, entries);
        sipWALWriter.write(tenantId, entries);
    }

    // ActivityWAL
    private static final ColumnKey<WALColumnKey> ACTIVITY_WAL_COLUMN_KEY = new ColumnKey<WALColumnKey>() {
        @Override
        public WALColumnKey provideColumnKey(WALEntry entry, CollisionId collisionId) {
            return new WALColumnKey(0, collisionId.collisionId(entry));
        }
    };

    private static final CollisionId ACTIVITY_WAL_COLLISIONID = new CollisionId() {
        @Override
        public long collisionId(MiruPartitionedActivity activity) {
            if (activity.type != MiruPartitionedActivity.Type.BEGIN && activity.type != MiruPartitionedActivity.Type.END) {
                return activity.timestamp;
            } else {
                return activity.writerId;
            }
        }
    };

    // ActivitySipWAL
    private static final ColumnKey<SipWALColumnKey> ACTIVITY_SIP_WAL_COLUMN_KEY = new ColumnKey<SipWALColumnKey>() {
        @Override
        public SipWALColumnKey provideColumnKey(MiruPartitionedActivity activity, CollisionId collisionId) {
            return new SipWALColumnKey(activity.type.getSort(), collisionId.collisionId(activity), activity.timestamp);
        }
    };

    private static final CollisionId ACTIVITY_SIP_WAL_COLLISIONID = new CollisionId() {
        @Override
        public long collisionId(MiruPartitionedActivity activity) {
            if (activity.type != MiruPartitionedActivity.Type.BEGIN && activity.type != MiruPartitionedActivity.Type.END) {
                return activity.clockTimestamp;
            } else {
                return activity.writerId;
            }
        }
    };
}
