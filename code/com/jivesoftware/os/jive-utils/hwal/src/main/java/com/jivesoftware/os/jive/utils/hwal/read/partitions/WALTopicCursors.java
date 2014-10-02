package com.jivesoftware.os.jive.utils.hwal.read.partitions;

import com.google.common.base.Optional;
import com.jivesoftware.os.jive.utils.hwal.read.WALCursorStore;
import com.jivesoftware.os.jive.utils.hwal.read.WALReaders;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import com.jivesoftware.os.jive.utils.permit.Permit;
import com.jivesoftware.os.jive.utils.permit.PermitConfig;
import com.jivesoftware.os.jive.utils.permit.PermitProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 *
 * @author jonathan
 */
public class WALTopicCursors {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();

    private final String cursorGroup;
    private final String topicId;
    private final WALReaders walReaders;
    private final PermitProvider topicCursorPermitProvider;
    private final PermitConfig topicCursorPermitConfig;
    private final WALCursorStore cursorStore;
    private final ConcurrentSkipListMap<Permit, WALTopicCursor> cursors = new ConcurrentSkipListMap<>();

    public WALTopicCursors(WALReaders walReaders,
            String cursorGroup,
            String topicId,
            PermitProvider topicCursorPermitProvider,
            PermitConfig topicCursorPermitConfig,
            WALCursorStore cursorStore) {
        this.cursorGroup = cursorGroup;
        this.topicId = topicId;
        this.cursorStore = cursorStore;
        this.walReaders = walReaders;
        this.topicCursorPermitProvider = topicCursorPermitProvider;
        this.topicCursorPermitConfig = topicCursorPermitConfig;
    }

    public Collection<WALTopicCursor> getCursors() {
        return cursors.values();
    }

    public List<Cursor> getAllCursors() {
        List<Permit> allIssuedPermits = topicCursorPermitProvider.getAllIssuedPermits(topicId, cursorGroup, topicCursorPermitConfig);
        List<Cursor> lags = new ArrayList<>();
        int totalNumberOfConcurrentPermits = topicCursorPermitConfig.getCountIds();
        String fullTopicName = topicId + "-" + totalNumberOfConcurrentPermits;
        for (Permit permit : allIssuedPermits) {
            long cursor = cursorStore.get(cursorGroup, fullTopicName, permit.id);
            lags.add(new Cursor(walReaders.getTenantId(), walReaders.getReaderGroupId(), cursorGroup, fullTopicName, permit.owner, permit.id, cursor));
        }
        return lags;
    }

    /**
     * Call this at some periodic interval
     */
    public void online() {
        renewExistingCursors();
        ensureAllPermitsAreTaken();
    }

    public void offline() {
        Map<Permit, WALTopicCursor> clear = new HashMap<>(cursors);
        cursors.clear();
        topicCursorPermitProvider.releasePermit(clear.keySet());
    }

    private void renewExistingCursors() {
        List<Permit> currentPermits = new ArrayList<>(cursors.keySet());
        List<Optional<Permit>> renewedPermits = topicCursorPermitProvider.renewPermit(currentPermits);
        for (int i = 0; i < currentPermits.size(); i++) {
            Optional<Permit> renewedPermit = renewedPermits.get(i);
            if (renewedPermit.isPresent()) {
                int totalNumberOfConcurrentPermits = topicCursorPermitConfig.getCountIds();
                Permit newPermit = renewedPermit.get();
                cursors.remove(currentPermits.get(i));
                WALTopicCursor cursor = new WALTopicCursor(cursorGroup, topicId + "-" + totalNumberOfConcurrentPermits,
                        new JITPartitionId(topicCursorPermitProvider, newPermit), cursorStore);
                cursors.put(newPermit, cursor);
                LOG.debug("Renewed permit:" + newPermit + " for cursor:" + cursor);
            } else {
                WALTopicCursor removed = cursors.remove(currentPermits.get(i));
                LOG.info("Dettached from:" + currentPermits.get(i) + " for cursor:" + removed);
            }
        }
    }

    private void ensureAllPermitsAreTaken() {
        int numberOfOnlineWALReaders = walReaders.getNumberOfOnlineWALReaders();
        if (numberOfOnlineWALReaders > 0) {

            int totalNumberOfConcurrentPermits = topicCursorPermitConfig.getCountIds();

            int desiredNumberOfPermits = (int) Math.ceil((double) totalNumberOfConcurrentPermits / (double) numberOfOnlineWALReaders);

            int currentNumberOfPermits = cursors.size();

            int askForNPermits = desiredNumberOfPermits - currentNumberOfPermits;
            if (askForNPermits > 0) {
                LOG.info("numberOfOnlineWALReaders:{} totalNumberOfConcurrentPartitions:{} desiredNumberOfPartition:{}"
                        + " for topic:{} currently have {} partitions", new Object[]{
                            numberOfOnlineWALReaders, totalNumberOfConcurrentPermits, desiredNumberOfPermits, topicId, currentNumberOfPermits
                        });

                List<Permit> requestedPermits = topicCursorPermitProvider.requestPermit(topicId, cursorGroup, topicCursorPermitConfig, askForNPermits);
                for (Permit permit : requestedPermits) {
                    WALTopicCursor cursor = new WALTopicCursor(cursorGroup, topicId + "-" + totalNumberOfConcurrentPermits,
                            new JITPartitionId(topicCursorPermitProvider, permit), cursorStore);
                    cursors.put(permit, cursor);
                    LOG.info("Attached permit:" + permit + " to cursor:" + cursor);
                }
            }

            if (cursors.size() > desiredNumberOfPermits) {
                int release = cursors.size() - desiredNumberOfPermits;
                LOG.info("readerGroup:{} for topic:{} releasing {} partitions.", new Object[]{cursorGroup, topicId, release});
                for (int i = 0; i < release; i++) {
                    Map.Entry<Permit, WALTopicCursor> entry = cursors.pollFirstEntry();
                    cursors.remove(entry.getKey());
                    topicCursorPermitProvider.releasePermit(Arrays.asList(entry.getKey()));
                }

            }
        }

    }

    static class JITPartitionId implements PartitionId {

        private final PermitProvider permitProvider;
        private final Permit permit;

        public JITPartitionId(PermitProvider permitProvider, Permit permit) {
            this.permitProvider = permitProvider;
            this.permit = permit;
        }

        @Override
        public Optional<Integer> getId() {

            Optional<Permit> optionalPermit = permitProvider.isExpired(permit);
            if (optionalPermit.isPresent()) {
                return Optional.of(optionalPermit.get().id);
            } else {
                return Optional.absent();
            }
        }
    }

}
