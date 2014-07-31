package com.jivesoftware.os.jive.utils.hwal.shared.partition;

import com.jivesoftware.os.jive.utils.hwal.shared.api.WALKey;
import com.jivesoftware.os.jive.utils.hwal.shared.api.WALKey;

/**
 *
 * @author jonathan.colt
 */
public interface WALParitioningStrategy {

    int parition(WALKey key, int numberOfPartitions);
}
