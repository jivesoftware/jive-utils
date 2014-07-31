package com.jivesoftware.os.jive.utils.hwal.read.partitions;

import com.google.common.base.Optional;
import com.jivesoftware.os.jive.utils.hwal.read.WALCursorStore;
import com.jivesoftware.os.jive.utils.hwal.shared.api.TopicId;
import com.jivesoftware.os.jive.utils.id.TenantId;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import com.jivesoftware.os.jive.utils.permit.Permit;
import com.jivesoftware.os.jive.utils.permit.PermitProvider;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author jonathan
 */
public class WALCursors {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();

    private final TenantId tenantId;
    private final TopicId topicId;
    private final WALReaders walReaders;
    private final PermitProvider cursorPermitProvider;
    private final WALCursorStore cursorStore;
    private final ConcurrentHashMap<Permit,WALCursor> cursors = new ConcurrentHashMap<>();

    public WALCursors(WALReaders walReaders,
            TenantId tenantId,
            TopicId topicId,
            PermitProvider cursorPermitProvider,
            WALCursorStore cursorStore) {
        this.tenantId = tenantId;
        this.topicId = topicId;
        this.cursorStore = cursorStore;
        this.walReaders = walReaders;
        this.cursorPermitProvider = cursorPermitProvider;
    }


    public Collection<WALCursor> getCursors() {
        return cursors.values();
    }

    public int getNumberOfParitions() {
        return cursorPermitProvider.getTotalNumberOfConcurrentPermits();
    }

    /**
     * Call this at some periodic interval
     */
    public void online() {

        int numberOfOnlineWALReaders = walReaders.getNumberOfOnlineWALReaders();
        int totalNumberOfConcurrentPermits = cursorPermitProvider.getTotalNumberOfConcurrentPermits();

        int desiredNumberOfPermits = (totalNumberOfConcurrentPermits/numberOfOnlineWALReaders) + 1;

        int currentNumberOfPermits = cursors.size();
        for(int i=0;i<desiredNumberOfPermits-currentNumberOfPermits;i++) {
            Optional<Permit> requestedPermit = cursorPermitProvider.requestPermit();
            if (requestedPermit.isPresent()) {
                Permit permit = requestedPermit.get();
                cursors.put(permit, new WALCursor(tenantId, topicId, new PartitionId(), cursorStore));
            }
        }
    }

    public void offline() {
        Map<Permit,WALCursor> clear = new HashMap<>(cursors);
        cursors.clear();
        for(Permit permit:clear.keySet()) {
            cursorPermitProvider.releasePermit(permit);
        }
    }



}
