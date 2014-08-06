package com.jivesoftware.os.jive.utils.hwal.read.rcvs;

import com.google.common.base.Optional;
import com.jivesoftware.os.jive.utils.base.interfaces.CallbackStream;
import com.jivesoftware.os.jive.utils.hwal.read.WALTopicReader;
import com.jivesoftware.os.jive.utils.hwal.read.partitions.Cursor;
import com.jivesoftware.os.jive.utils.hwal.read.partitions.TopicLag;
import com.jivesoftware.os.jive.utils.hwal.read.partitions.WALTopicCursor;
import com.jivesoftware.os.jive.utils.hwal.read.partitions.WALTopicCursors;
import com.jivesoftware.os.jive.utils.hwal.shared.api.SipWALEntry;
import com.jivesoftware.os.jive.utils.hwal.shared.api.SipWALTime;
import com.jivesoftware.os.jive.utils.hwal.shared.api.WALEntry;
import com.jivesoftware.os.jive.utils.hwal.shared.filter.WALKeyFilter;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.ColumnValueAndTimestamp;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.lang.mutable.MutableLong;

/**
 * @author jonathan
 */
public class RCVSWALTopicReader implements WALTopicReader {

    private final MetricLogger LOG = MetricLoggerFactory.getLogger();

    private final RowColumnValueStore<String, Integer, Long, WALEntry, ? extends Exception> wal;
    private final RowColumnValueStore<String, Integer, SipWALTime, SipWALEntry, ? extends Exception> sipWAL;
    private final WALTopicCursors cursors;
    private final long pollEmptyPartitionIntervalMillis;
    private final long maxClockDriftInMillis;

    public RCVSWALTopicReader(RowColumnValueStore<String, Integer, Long, WALEntry, ? extends Exception> wal,
            RowColumnValueStore<String, Integer, SipWALTime, SipWALEntry, ? extends Exception> sipWAL,
            WALTopicCursors cursors,
            long pollEmptyPartitionIntervalMillis,
            long maxClockDriftInMillis) {
        this.wal = wal;
        this.sipWAL = sipWAL;
        this.cursors = cursors;
        this.pollEmptyPartitionIntervalMillis = pollEmptyPartitionIntervalMillis;
        this.maxClockDriftInMillis = maxClockDriftInMillis;
    }

    @Override
    public List<TopicLag> getTopicLags() throws Exception {
        List<Cursor> allCursors = cursors.getAllCursors();
        List<TopicLag> topicLags = new ArrayList();
        for (final Cursor cursor : allCursors) {
            SipWALTime sipWALTime = new SipWALTime(cursor.cursor, 0);
            final MutableLong latestTimestamp = new MutableLong(cursor.cursor);
            final MutableInt pending = new MutableInt();
            sipWAL.getEntrys(cursor.topic, cursor.partition, sipWALTime, 100000L, 10000, false, null, null,
                    new CallbackStream<ColumnValueAndTimestamp<SipWALTime, SipWALEntry, Long>>() {

                        @Override
                        public ColumnValueAndTimestamp<SipWALTime, SipWALEntry, Long> callback(ColumnValueAndTimestamp<SipWALTime, SipWALEntry, Long> v)
                                throws Exception {
                            if (v != null) {
                                long timestamp = v.getColumn().getTimestamp();
                                if (timestamp > cursor.cursor) {
                                    pending.increment();
                                }
                                if (timestamp > latestTimestamp.longValue()) {
                                    latestTimestamp.setValue(timestamp);
                                }
                            }
                            return v;
                        }
                    });
            topicLags.add(new TopicLag(cursor, latestTimestamp.longValue(), pending.intValue()));
        }
        return topicLags;
    }


    @Override
    public void stream(final WALKeyFilter filter, final int batchSize, final WALTopicStream stream) throws Exception {
        try {
            final ConcurrentSkipListSet dedupper = new ConcurrentSkipListSet();
            Map<Integer, Long> lastNonEmptyTimestamps = new ConcurrentHashMap<>();
            while (true) { // TODO burp
                int numberOfNonEmptyPartitions = 0;
                for (WALTopicCursor cursor : cursors.getCursors()) {
                    Optional<Integer> partition = cursor.getPartition();
                    if (!partition.isPresent()) {
                        LOG.info("Skipped expired partition:" + cursor);
                        continue;
                    }

                    Long lastNonEmptyTimestamp = lastNonEmptyTimestamps.get(partition.get());
                    if (lastNonEmptyTimestamp != null
                            && System.currentTimeMillis() - lastNonEmptyTimestamp < pollEmptyPartitionIntervalMillis) {
                        LOG.debug("Skipped idle parition:" + cursor);
                        continue;
                    }

                    final Optional<Long> currentOffset = cursor.currentOffest();
                    if (!currentOffset.isPresent()) {
                        LOG.info("Skipped do to lack of offset:" + cursor);
                        continue;
                    }

                    final List<Long> entryIds = new ArrayList<>();
                    final MutableLong maxOffset = new MutableLong(currentOffset.get());
                    SipWALTime sipWALTime = new SipWALTime(currentOffset.get() - maxClockDriftInMillis, 0);
                    sipWAL.getEntrys(cursor.getTopicId(), partition.get(), sipWALTime, 100000L, batchSize, false, null, null,
                            new CallbackStream<ColumnValueAndTimestamp<SipWALTime, SipWALEntry, Long>>() {

                                @Override
                                public ColumnValueAndTimestamp<SipWALTime, SipWALEntry, Long> callback(
                                        ColumnValueAndTimestamp<SipWALTime, SipWALEntry, Long> value) throws Exception {
                                            if (value != null) {
                                                SipWALEntry sipEntry = value.getValue();
                                                if (!filter.include(sipEntry.key)) {
                                                    return value;
                                                }
                                                long uniqueOrderingId = sipEntry.uniqueOrderingId;
                                                if (dedupper.contains(uniqueOrderingId)) {
                                                    return value;
                                                }

                                                SipWALTime sipWALTime = value.getColumn();

                                                entryIds.add(uniqueOrderingId);
                                                if (maxOffset.longValue() < sipWALTime.getTimestamp()) {
                                                    maxOffset.setValue(sipWALTime.getTimestamp());
                                                }
                                                if (entryIds.size() > batchSize && sipWALTime.getTimestamp() > currentOffset.get()) {
                                                    LOG.info("Batch full. " + batchSize);
                                                    return null; // stop consuming
                                                }
                                            }
                                            return value;
                                        }
                            });

                    if (entryIds.isEmpty()) {
                        lastNonEmptyTimestamps.put(partition.get(), System.currentTimeMillis());
                    } else {
                        numberOfNonEmptyPartitions++;
                        lastNonEmptyTimestamps.put(partition.get(), 0L);
                        //LOG.debug("Get " + cursor.getTopicId() + " " + partition.get() + " " + Arrays.toString(entryIds.toArray(new Long[entryIds.size()])));
                        List<WALEntry> entries = wal.multiGet(cursor.getTopicId(), partition.get(), entryIds.toArray(new Long[entryIds.size()]), null, null);
                        try {
                            LOG.debug("Streaming " + entryIds.size() + " from topic:" + cursor.getTopicId() + " from partition:" + partition.get());
                            stream.stream(cursor.getTopicId(), partition.get(), entries);
                        } catch (Exception x) {
                            LOG.error("Provided walStream threw the following exception "
                                    + "while handling the following:"
                                    + " topicId=" + cursor.getTopicId()
                                    + " partitionId=" + partition.get()
                                    + " entryIds=" + entryIds, x);
                        }
                        cursor.commit(maxOffset.longValue());
                        dedupper.addAll(entryIds);
                    }

                }

                if (numberOfNonEmptyPartitions == 0) {
                    Thread.sleep(pollEmptyPartitionIntervalMillis); //TODO just like above "burp". Fix to use schedualed executor
                }

                int maxDedupperCapacity = 10_000; // TODO Expose to config
                if (dedupper.size() > maxDedupperCapacity) { // HACK
                    for (int i = 0; i < maxDedupperCapacity / 2; i++) {
                        dedupper.pollFirst();
                    }
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

}
