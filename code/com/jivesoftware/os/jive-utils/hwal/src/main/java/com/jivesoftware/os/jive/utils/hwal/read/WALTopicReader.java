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
         * @param entries
         * @return
         * @throws Exception
         */
        void stream(List<WALEntry> entries);
    }

    void stream(WALKeyFilter filter, int batchSize, WALTopicStream stream) throws Exception;


}
