package com.jivesoftware.os.jive.utils.hwal.read;

import com.jivesoftware.os.jive.utils.hwal.shared.api.WALEntry;
import com.jivesoftware.os.jive.utils.hwal.shared.api.TopicId;
import com.jivesoftware.os.jive.utils.hwal.shared.filter.WALKeyFilter;
import com.jivesoftware.os.jive.utils.id.TenantId;
import java.util.List;

/**
 * @author jonathan
 */
public interface WALReader {

    interface WALStream {
        /**
         *
         * @param entries
         * @return
         * @throws Exception
         */
        void stream(List<WALEntry> entries);
    }

    void stream(TenantId tenantId, TopicId topicId, WALKeyFilter filter, int batchSize, WALStream stream) throws Exception;


}
