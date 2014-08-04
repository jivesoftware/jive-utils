package com.jivesoftware.os.jive.utils.hwal.write.rcvs;

import com.jivesoftware.os.jive.utils.hwal.shared.api.SipWALEntry;
import com.jivesoftware.os.jive.utils.hwal.shared.api.SipWALTime;
import com.jivesoftware.os.jive.utils.hwal.shared.api.WALEntry;
import com.jivesoftware.os.jive.utils.hwal.shared.partition.WALPartitioningStrategy;
import com.jivesoftware.os.jive.utils.hwal.write.SipWALTimeProvider;
import com.jivesoftware.os.jive.utils.hwal.write.WALWriter;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.MultiAdd;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumValueTimestampAdd;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.ConstantTimestamper;
import java.util.Collection;
import java.util.List;

/**
 * @author jonathan
 */
public class RCVSWALWriter implements WALWriter {

    private final MetricLogger LOG = MetricLoggerFactory.getLogger();

    private final SipWALTimeProvider sipWALTimeProvider;
    private final RowColumnValueStore<String, Integer, Long, WALEntry, ? extends Exception> wal;
    private final RowColumnValueStore<String, Integer, SipWALTime, SipWALEntry, ? extends Exception> sipWAL;
    private final WALPartitioningStrategy paritioningStrategy;
    private final int numberOfPartitions;

    public RCVSWALWriter(SipWALTimeProvider sipWALTimeProvider, RowColumnValueStore<String, Integer, Long, WALEntry, ? extends Exception> wal,
            RowColumnValueStore<String, Integer, SipWALTime, SipWALEntry, ? extends Exception> sipWAL,
            WALPartitioningStrategy paritioningStrategy,
            int numberOfPartitions) {
        this.sipWALTimeProvider = sipWALTimeProvider;
        this.wal = wal;
        this.sipWAL = sipWAL;
        this.paritioningStrategy = paritioningStrategy;
        this.numberOfPartitions = numberOfPartitions;
    }

    @Override
    public void write(String topicId, Collection<WALEntry> entries) throws Exception {
        MultiAdd<Integer, Long, WALEntry> walAdds = new MultiAdd<>();
        MultiAdd<Integer, SipWALTime, SipWALEntry> sipWALAdds = new MultiAdd<>();
        for (WALEntry entry : entries) {
            SipWALEntry sipWALEntry = entry.getSipWALEntry();
            int partition = paritioningStrategy.partition(sipWALEntry.key, numberOfPartitions);
            walAdds.add(
                    partition,
                    sipWALEntry.uniqueOrderingId,
                    entry,
                    new ConstantTimestamper(sipWALEntry.ingressedTimestampMillis)
            );

            sipWALAdds.add(
                    partition,
                    sipWALTimeProvider.currentSipTime(), // TODO replace with a HBase co-processor. This current implementation is vulnerable to GC pauses.
                    sipWALEntry,
                    new ConstantTimestamper(sipWALEntry.ingressedTimestampMillis)
            );
        }

        String paritionedTopicId = topicId + "-" + numberOfPartitions; // this allows us to change the number of paritions on the fly.
        List<RowColumValueTimestampAdd<Integer, Long, WALEntry>> tookWalAdds = walAdds.take();
        wal.multiRowsMultiAdd(paritionedTopicId, tookWalAdds);
        //LOG.info("Wrote to WAL "+tookWalAdds);

        List<RowColumValueTimestampAdd<Integer, SipWALTime, SipWALEntry>> tookSipWalAdds = sipWALAdds.take();
        sipWAL.multiRowsMultiAdd(paritionedTopicId, tookSipWalAdds);
        //LOG.info("Wrote to sip WAL "+tookSipWalAdds);

    }

}
