package com.jivesoftware.os.jive.utils.hwal.read.partitions;

/**
 *
 * @author jonathan
 */
public class WALCursor implements Comparable<WALCursor> {
    private final int partition;

    public WALCursor(int partition) {
        this.partition = partition;
    }


    public int getPartition() {
        return partition;
    }

    public long currentOffest() {
        return -1;
    }

    public void commit(long offest) {

    }

    @Override
    public int compareTo(WALCursor o) {
        return Integer.compare(partition, o.partition);
    }

}
