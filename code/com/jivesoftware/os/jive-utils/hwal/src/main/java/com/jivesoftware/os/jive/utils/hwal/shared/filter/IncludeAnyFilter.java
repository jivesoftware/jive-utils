package com.jivesoftware.os.jive.utils.hwal.shared.filter;

/**
 *
 * @author jonathan
 */
public class IncludeAnyFilter implements WALKeyFilter {

    @Override
    public boolean include(byte[] key) {
        return true;
    }

}
