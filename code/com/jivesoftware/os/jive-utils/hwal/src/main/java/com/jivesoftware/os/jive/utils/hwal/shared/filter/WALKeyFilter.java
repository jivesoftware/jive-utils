package com.jivesoftware.os.jive.utils.hwal.shared.filter;

import com.jivesoftware.os.jive.utils.hwal.shared.api.WALKey;

/**
 *
 * @author jonathan
 */
public interface WALKeyFilter {

    boolean include(WALKey key);
}
