package com.jivesoftware.os.jive.utils.hwal.shared.hbase;

public class WALRow implements Comparable<WALRow> {
    private final int partitionId;

    public WALRow(int partitionId) {
        this.partitionId = partitionId;
    }

    public int getPartitionId() {
        return partitionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WALRow that = (WALRow) o;

        if (partitionId != that.partitionId) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return partitionId;
    }

    @Override
    public int compareTo(WALRow miruActivityWALRow) {
        return Integer.compare(partitionId, miruActivityWALRow.partitionId);
    }
}
