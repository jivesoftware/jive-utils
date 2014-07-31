package com.jivesoftware.os.jive.utils.hwal.read.rcvs;

import com.jivesoftware.os.jive.utils.base.interfaces.CallbackStream;
import com.jivesoftware.os.jive.utils.hwal.read.WALReader;
import com.jivesoftware.os.jive.utils.hwal.read.partitions.WALCursor;
import com.jivesoftware.os.jive.utils.hwal.read.partitions.WALCursors;
import com.jivesoftware.os.jive.utils.hwal.shared.api.SipWALEntry;
import com.jivesoftware.os.jive.utils.hwal.shared.api.TopicId;
import com.jivesoftware.os.jive.utils.hwal.shared.api.WALEntry;
import com.jivesoftware.os.jive.utils.hwal.shared.api.TopicPartition;
import com.jivesoftware.os.jive.utils.hwal.shared.filter.WALKeyFilter;
import com.jivesoftware.os.jive.utils.id.TenantId;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.ColumnValueAndTimestamp;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.apache.commons.lang.mutable.MutableLong;

/**
 * @author jonathan
 */
public class RCVSWALReader implements WALReader {

    private final MetricLogger LOG = MetricLoggerFactory.getLogger();

    private final RowColumnValueStore<TenantId, TopicPartition, Long, WALEntry, ? extends Exception> wal;
    private final RowColumnValueStore<TenantId, TopicPartition, Long, SipWALEntry, ? extends Exception> sipWAL;
    private final WALCursors cursors;
    private final long pollingIntervalMillis;
    private final long maxClockDriftInMillis;

    public RCVSWALReader(RowColumnValueStore<TenantId, TopicPartition, Long, WALEntry, ? extends Exception> wal,
            RowColumnValueStore<TenantId, TopicPartition, Long, SipWALEntry, ? extends Exception> sipWAL,
            WALCursors cursors,
            long pollingIntervalMillis,
            long maxClockDriftInMillis) {
        this.wal = wal;
        this.sipWAL = sipWAL;
        this.cursors = cursors;
        this.pollingIntervalMillis = pollingIntervalMillis;
        this.maxClockDriftInMillis = maxClockDriftInMillis;
    }

    @Override
    public void stream(TenantId tenantId, TopicId topicId, final WALKeyFilter filter, final int batchSize, final WALStream stream) throws Exception {

        final ConcurrentSkipListSet dedupper = new ConcurrentSkipListSet();
        while (true) { // TODO burp
            int numberOfNonEmptyPartitions = 0;
            Map<Integer, Long> lastNonEmptyTimestamps = new ConcurrentHashMap<>();
            for (WALCursor cursor : cursors.getCursors()) {

                Long lastNonEmptyTimestamp = lastNonEmptyTimestamps.get(cursor.getPartition());
                if (lastNonEmptyTimestamp == null || lastNonEmptyTimestamp < (System.currentTimeMillis() + pollingIntervalMillis)) {

                    TopicPartition rowKey = new TopicPartition(cursor.getPartition(), topicId.getId());

                    final List<Long> entryIds = new ArrayList<>();
                    final long currentOffset = cursor.currentOffest();
                    final MutableLong maxOffset = new MutableLong(currentOffset);
                    sipWAL.getEntrys(tenantId, rowKey, currentOffset - maxClockDriftInMillis, 100000L, batchSize, false, null, null,
                            new CallbackStream<ColumnValueAndTimestamp<Long, SipWALEntry, Long>>() {

                                @Override
                                public ColumnValueAndTimestamp<Long, SipWALEntry, Long> callback(ColumnValueAndTimestamp<Long, SipWALEntry, Long> value) throws Exception {
                                    if (value != null) {
                                        if (filter.include(value.getValue().key)) {
                                            long uniqueOrderingId = value.getValue().uniqueOrderingId;
                                            if (!dedupper.contains(uniqueOrderingId)) {
                                                entryIds.add(uniqueOrderingId);
                                                dedupper.add(uniqueOrderingId);
                                                if (maxOffset.longValue() < value.getColumn()) {
                                                    maxOffset.setValue(value.getColumn());
                                                }
                                                if (entryIds.size() > batchSize && value.getColumn() > currentOffset) {
                                                    return null; // stop consuming
                                                }
                                            }
                                        }
                                    }
                                    return value;
                                }
                            });

                    if (entryIds.isEmpty()) {
                        lastNonEmptyTimestamps.put(cursor.getPartition(), System.currentTimeMillis());
                    } else {
                        numberOfNonEmptyPartitions++;
                        lastNonEmptyTimestamps.put(cursor.getPartition(), 0L);
                        List<WALEntry> entries = wal.multiGet(tenantId, rowKey, entryIds.toArray(new Long[entryIds.size()]), null, null);
                        try {
                            stream.stream(entries);
                        } catch (Exception x) {
                            LOG.error("Provided walStream threw the following exception "
                                    + "while handling the following:"
                                    + " tenantId=" + tenantId
                                    + " topicId=" + topicId
                                    + " entryIds=" + entryIds, x);
                        }
                        cursor.commit(maxOffset.longValue());
                    }
                }
            }

            if (numberOfNonEmptyPartitions == 0) {
                Thread.sleep(pollingIntervalMillis); //TODO just like above "burp". Fix to use schedualed executor
            }

            int maxDedupperCapacity = 10_000;
            if (dedupper.size() > maxDedupperCapacity) { // HACK
                for (int i = 0; i < maxDedupperCapacity / 2; i++) {
                    dedupper.pollFirst();
                }
            }
        }
    }

}
