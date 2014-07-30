package com.jivesoftware.os.jive.utils.hwal.shared;

/**
 *
 * @author jonathan.colt
 * @param <P>
 */
public interface WALParitioningStrategy<P> {

    int parition(P partitionKey, int numberOfPartitions);
}
