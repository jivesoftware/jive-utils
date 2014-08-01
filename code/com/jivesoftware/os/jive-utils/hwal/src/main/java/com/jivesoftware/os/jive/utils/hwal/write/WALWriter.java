package com.jivesoftware.os.jive.utils.hwal.write;

import com.jivesoftware.os.jive.utils.hwal.shared.api.WALEntry;
import java.util.Collection;

/**
 * @author jonathan
 */
public interface WALWriter {

    void write(String topicId, Collection<WALEntry> entries) throws Exception;

}
