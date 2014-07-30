package com.jivesoftware.os.jive.utils.hwal.shared.hbase;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

/** @author jonathan */
public class WALEntry {
    public final int writerId;
    public final TopicId topicId;
    public final byte[] tenantId;
    public final int index;
    public final long timestamp;
    public final long clockTimestamp;
    public final WALPayload payload;

    /** Construct using {@link MiruPartitionedActivityFactory}. */
    WALEntry(int writerId, TopicId topicId, byte[] tenantId, int index, long timestamp,
        long clockTimestamp, WALPayload payload) {
        this.writerId = writerId;
        this.topicId = topicId;
        this.tenantId = tenantId;
        this.index = index;
        this.timestamp = timestamp;
        this.clockTimestamp = clockTimestamp;
        this.payload = payload;
    }

    /** Subverts the package constructor, but at least you'll feel dumb using a method called <code>fromJson</code>. */
    @JsonCreator
    public static WALEntry fromJson(
        @JsonProperty ("writerId") int writerId,
        @JsonProperty ("partitionId") int partitionId,
        @JsonProperty ("tenantId") byte[] tenantId,
        @JsonProperty ("index") int index,
        @JsonProperty ("timestamp") long timestamp,
        @JsonProperty ("clock") long clockTimestamp,
        @JsonProperty ("payload") WALPayload payload) {
        return new WALEntry(writerId, TopicId.of(partitionId), tenantId, index, timestamp,
            clockTimestamp, payload);
    }

    @JsonGetter ("payload")
    public WALPayload getPayload() {
        return payload;
    }

    @JsonGetter ("topicId")
    public int getTopicId() {
        return topicId.getId();
    }

}
