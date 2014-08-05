package com.jivesoftware.os.jive.utils.hwal.read;

import com.jivesoftware.os.jive.utils.hwal.shared.api.WALEntry;
import com.jivesoftware.os.jive.utils.hwal.shared.filter.WALKeyFilter;
import java.util.List;

/**
 * @author jonathan
 */
public interface WALTopicReader {

    interface WALTopicStream {

        /**
         *
         * @param topic
         * @param partition
         * @param entries
         */
        void stream(String topic, int partition, List<WALEntry> entries);
    }

    void stream(WALKeyFilter filter, int batchSize, WALTopicStream stream) throws Exception;

}
