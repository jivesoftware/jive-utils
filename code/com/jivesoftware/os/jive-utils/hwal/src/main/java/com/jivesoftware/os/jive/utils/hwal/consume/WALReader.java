package com.jivesoftware.os.jive.utils.hwal.consume;

import com.jivesoftware.os.jive.utils.hwal.shared.WALEntry;
import java.util.List;

/**
 *
 * @author jonathan.colt
 */
public interface WALReader {

    /**
     * When this method returns or throws an exception it is assumed that you have successfully processed the list of provided entries
     * @param entries
     */
    void stream(List<WALEntry> entries);
}
