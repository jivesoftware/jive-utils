package com.jivesoftware.os.jive.utils.hwal.shared.api;

public class TopicPartition implements Comparable<TopicPartition> {
    private final int partitionId;
    private final int topicId;

    public TopicPartition(int partitionId, int topicId) {
        this.partitionId = partitionId;
        this.topicId = topicId;
    }

    public int getPartitionId() {
        return partitionId;
    }

    public int getTopicId() {
        return topicId;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + this.partitionId;
        hash = 19 * hash + this.topicId;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TopicPartition other = (TopicPartition) obj;
        if (this.partitionId != other.partitionId) {
            return false;
        }
        if (this.topicId != other.topicId) {
            return false;
        }
        return true;
    }


    @Override
    public int compareTo(TopicPartition miruActivityWALRow) {
        int i = Integer.compare(partitionId, miruActivityWALRow.partitionId);
        if (i == 0) {
            i = Integer.compare(topicId, miruActivityWALRow.topicId);
        }
        return i;
    }
}
