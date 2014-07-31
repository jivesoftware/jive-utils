package com.jivesoftware.os.jive.utils.hwal.write.rcvs;

import com.jivesoftware.os.jive.utils.hwal.shared.api.TopicPartition;
import com.jivesoftware.os.jive.utils.hwal.shared.api.WALEntry;
import com.jivesoftware.os.jive.utils.hwal.shared.api.SipWALEntry;
import com.jivesoftware.os.jive.utils.hwal.shared.partition.WALParitioningStrategy;
import com.jivesoftware.os.jive.utils.hwal.write.WALWriter;
import com.jivesoftware.os.jive.utils.hwal.shared.api.TopicId;
import com.jivesoftware.os.jive.utils.hwal.write.WALWriter;
import com.jivesoftware.os.jive.utils.id.TenantId;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.MultiAdd;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.ConstantTimestamper;
import java.util.Collection;

/**
 * @author jonathan
 */
public class WALWriterImpl implements WALWriter {

    private final RowColumnValueStore<TenantId, TopicPartition, Long, WALEntry, ? extends Exception> wal;
    private final RowColumnValueStore<TenantId, TopicPartition, Long, SipWALEntry, ? extends Exception> sipWAL;
    private final WALParitioningStrategy paritioningStrategy;
    private final int numberOfPartitions;

    public WALWriterImpl(RowColumnValueStore<TenantId, TopicPartition, Long, WALEntry, ? extends Exception> wal,
            RowColumnValueStore<TenantId, TopicPartition, Long, SipWALEntry, ? extends Exception> sipWAL,
            WALParitioningStrategy paritioningStrategy,
            int numberOfPartitions) {
        this.wal = wal;
        this.sipWAL = sipWAL;
        this.paritioningStrategy = paritioningStrategy;
        this.numberOfPartitions = numberOfPartitions;
    }

    @Override
    public void write(TenantId tenantId, TopicId topicId, Collection<WALEntry> entries) throws Exception {
        MultiAdd<TopicPartition, Long, WALEntry> walAdds = new MultiAdd<>();
        MultiAdd<TopicPartition, Long, SipWALEntry> sipWALAdds = new MultiAdd<>();
        for (WALEntry entry : entries) {
            int partition = paritioningStrategy.parition(entry.key, numberOfPartitions);
            TopicPartition walRow = new TopicPartition(partition, topicId.getId());
            walAdds.add(
                    walRow,
                    entry.uniqueOrderingId,
                    entry,
                    new ConstantTimestamper(entry.timestamp)
            );

            sipWALAdds.add(
                    walRow,
                    System.currentTimeMillis(), // TODO replace with a HBase co-processor. This current implementation is vulnerable to GC pauses.
                    new SipWALEntry(partition, entry.timestamp, entry.key),
                    new ConstantTimestamper(entry.timestamp)
            );
        }

        wal.multiRowsMultiAdd(tenantId, walAdds.take());
        sipWAL.multiRowsMultiAdd(tenantId, sipWALAdds.take());
    }

}
