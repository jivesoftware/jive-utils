package com.jivesoftware.os.jive.utils.hwal.shared.hbase;

import com.jivesoftware.os.jive.utils.id.TenantId;

/** @author jonathan */
public interface WALReader {

    interface StreamWAL {

        boolean stream(long collisionId, WALEntry entry, long timestamp) throws Exception;
    }

    void stream(TenantId tenantId, TopicId topicId, long afterTimestamp, StreamWAL streamWAL)
        throws Exception;

    void streamSip(TenantId tenantId, TopicId topicId, long afterTimestamp, StreamWAL streamWAL)
        throws Exception;

    WALEntry findExisting(TenantId tenantId, TopicId topicId, WALEntry entry) throws Exception;

}
