package com.jivesoftware.os.jive.utils.hwal.write.rcvs;

import com.jivesoftware.os.jive.utils.hwal.shared.api.SipWALEntry;
import com.jivesoftware.os.jive.utils.hwal.shared.api.WALEntry;
import com.jivesoftware.os.jive.utils.hwal.shared.partition.WALParitioningStrategy;
import com.jivesoftware.os.jive.utils.hwal.write.WALWriter;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.MultiAdd;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.ConstantTimestamper;
import java.util.Collection;

/**
 * @author jonathan
 */
public class RCVSWALWriter implements WALWriter {

    private final RowColumnValueStore<String, Integer, Long, WALEntry, ? extends Exception> wal;
    private final RowColumnValueStore<String, Integer, Long, SipWALEntry, ? extends Exception> sipWAL;
    private final WALParitioningStrategy paritioningStrategy;
    private final int numberOfPartitions;

    public RCVSWALWriter(RowColumnValueStore<String, Integer, Long, WALEntry, ? extends Exception> wal,
            RowColumnValueStore<String, Integer, Long, SipWALEntry, ? extends Exception> sipWAL,
            WALParitioningStrategy paritioningStrategy,
            int numberOfPartitions) {
        this.wal = wal;
        this.sipWAL = sipWAL;
        this.paritioningStrategy = paritioningStrategy;
        this.numberOfPartitions = numberOfPartitions;
    }

    @Override
    public void write(String topicId, Collection<WALEntry> entries) throws Exception {
        MultiAdd<Integer, Long, WALEntry> walAdds = new MultiAdd<>();
        MultiAdd<Integer, Long, SipWALEntry> sipWALAdds = new MultiAdd<>();
        for (WALEntry entry : entries) {
            int partition = paritioningStrategy.parition(entry.key, numberOfPartitions);
            walAdds.add(
                    partition,
                    entry.uniqueOrderingId,
                    entry,
                    new ConstantTimestamper(entry.timestamp)
            );

            sipWALAdds.add(
                    partition,
                    System.currentTimeMillis(), // TODO replace with a HBase co-processor. This current implementation is vulnerable to GC pauses.
                    new SipWALEntry(partition, entry.timestamp, entry.key),
                    new ConstantTimestamper(entry.timestamp)
            );
        }

        String paritionedTopicId = topicId + "-" + numberOfPartitions; // this allows us to change the number of paritions on the fly.
        wal.multiRowsMultiAdd(paritionedTopicId, walAdds.take());
        sipWAL.multiRowsMultiAdd(paritionedTopicId, sipWALAdds.take());
    }

}
