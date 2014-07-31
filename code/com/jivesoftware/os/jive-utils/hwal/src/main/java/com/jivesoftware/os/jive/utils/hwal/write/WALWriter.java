package com.jivesoftware.os.jive.utils.hwal.write;

import com.jivesoftware.os.jive.utils.hwal.shared.api.WALEntry;
import com.jivesoftware.os.jive.utils.hwal.shared.api.TopicId;
import com.jivesoftware.os.jive.utils.id.TenantId;
import java.util.Collection;

/**
 * @author jonathan
 */
public interface WALWriter {

    void write(TenantId tenantId, TopicId topicId, Collection<WALEntry> entries) throws Exception;

}
