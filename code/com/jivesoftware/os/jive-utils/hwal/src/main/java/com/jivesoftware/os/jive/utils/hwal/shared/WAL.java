package com.jivesoftware.os.jive.utils.hwal.shared;

import java.util.Collection;

/**
 *
 * @author jonathan.colt
 */
public interface WAL<P, V> {

    void write(Collection<WALEntry<P, V>> entries);
}
