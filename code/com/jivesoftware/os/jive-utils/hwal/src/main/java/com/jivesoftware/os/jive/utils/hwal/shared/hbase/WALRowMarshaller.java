package com.jivesoftware.os.jive.utils.hwal.shared.hbase;

import com.jivesoftware.os.jive.utils.hwal.shared.api.TopicPartition;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.api.TypeMarshaller;
import java.nio.ByteBuffer;

public class WALRowMarshaller implements TypeMarshaller<TopicPartition> {

    @Override
    public TopicPartition fromBytes(byte[] bytes) throws Exception {
        int partitionId = ByteBuffer.wrap(bytes).getInt();
        int topicId = ByteBuffer.wrap(bytes).getInt();

        return new TopicPartition(partitionId, topicId);
    }

    @Override
    public byte[] toBytes(TopicPartition walRow) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(8);

        buffer.putInt(walRow.getPartitionId());
        buffer.putInt(walRow.getTopicId());

        return buffer.array();
    }

    @Override
    public TopicPartition fromLexBytes(byte[] bytes) throws Exception {
        return fromBytes(bytes);
    }

    @Override
    public byte[] toLexBytes(TopicPartition walRow) throws Exception {
        return toBytes(walRow);
    }
}
