package com.jivesoftware.os.jive.utils.hwal.shared.filter;

/**
 *
 * @author jonathan
 */
public interface WALKeyFilter {

    boolean include(byte[] key);
}
