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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author jonathan
 */
public class WALTopicCursors {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();

    private final String topicId;
    private final WALReaders walReaders;
    private final PermitProvider topicCursorPermitProvider;
    private final PermitConfig topicCursorPermitConfig;
    private final WALCursorStore cursorStore;
    private final ConcurrentHashMap<Permit, WALTopicCursor> cursors = new ConcurrentHashMap<>();

    public WALTopicCursors(WALReaders walReaders,
            String topicId,
            PermitProvider topicCursorPermitProvider,
            PermitConfig topicCursorPermitConfig,
            WALCursorStore cursorStore) {
        this.topicId = topicId;
        this.cursorStore = cursorStore;
        this.walReaders = walReaders;
        this.topicCursorPermitProvider = topicCursorPermitProvider;
        this.topicCursorPermitConfig = topicCursorPermitConfig;
    }

    public Collection<WALTopicCursor> getCursors() {
        return cursors.values();
    }

    public int getNumberOfParitions(String tenant) {
        return topicCursorPermitConfig.getCountIds();
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
                WALTopicCursor cursor = new WALTopicCursor(topicId + "-" + totalNumberOfConcurrentPermits, new JITPartitionId(topicCursorPermitProvider, newPermit), cursorStore);
                cursors.put(newPermit, cursor);
                LOG.debug("Renewed permit:"+newPermit+" for cursor:"+cursor);
            } else {
                WALTopicCursor removed = cursors.remove(currentPermits.get(i));
                LOG.info("Dettached from:"+currentPermits.get(i)+" for cursor:"+removed);
            }
        }
    }

    private void ensureAllPermitsAreTaken() {
        int numberOfOnlineWALReaders = walReaders.getNumberOfOnlineWALReaders();
        int totalNumberOfConcurrentPermits = topicCursorPermitConfig.getCountIds();

        int desiredNumberOfPermits = (int)Math.ceil((double)totalNumberOfConcurrentPermits / (double)numberOfOnlineWALReaders);

        int currentNumberOfPermits = cursors.size();

        int askForNPermits = desiredNumberOfPermits - currentNumberOfPermits;
        if (askForNPermits > 0) {
            LOG.info("asking for " + askForNPermits + " partitions for topic:" + topicId + " currently have " + currentNumberOfPermits + " paritions");

            List<Permit> requestedPermits = topicCursorPermitProvider.requestPermit(topicId, topicCursorPermitConfig, askForNPermits);
            for (Permit permit : requestedPermits) {
                WALTopicCursor cursor = new WALTopicCursor(topicId + "-" + totalNumberOfConcurrentPermits, new JITPartitionId(topicCursorPermitProvider, permit), cursorStore);
                cursors.put(permit, cursor);
                LOG.info("Atteched permit:"+permit+" to cursor:"+cursor);
            }
        }

        if (cursors.size() > desiredNumberOfPermits) {
            LOG.info("TODO: release paritions for topic:" + topicId);
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
            if (permitProvider.isPermitStillValid(permit)) {
                return Optional.of(permit.id);
            } else {
                return Optional.absent();
            }
        }

    }

}
