package com.jivesoftware.os.jive.utils.hwal.shared.filter;

import com.jivesoftware.os.jive.utils.hwal.shared.api.WALKey;

/**
 *
 * @author jonathan
 */
public class IncludeAnyFilter implements WALKeyFilter {

    @Override
    public boolean include(WALKey key) {
        return true;
    }

}
