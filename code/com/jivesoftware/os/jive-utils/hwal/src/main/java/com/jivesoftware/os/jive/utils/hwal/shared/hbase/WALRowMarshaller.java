package com.jivesoftware.os.jive.utils.hwal.shared.hbase;

import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.api.TypeMarshaller;
import java.nio.ByteBuffer;

public class WALRowMarshaller implements TypeMarshaller<WALRow> {

    @Override
    public WALRow fromBytes(byte[] bytes) throws Exception {
        int partitionId = ByteBuffer.wrap(bytes).getInt();

        return new WALRow(partitionId);
    }

    @Override
    public byte[] toBytes(WALRow miruActivityWALRow) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(4);

        buffer.putInt(miruActivityWALRow.getPartitionId());

        return buffer.array();
    }

    @Override
    public WALRow fromLexBytes(byte[] bytes) throws Exception {
        return fromBytes(bytes);
    }

    @Override
    public byte[] toLexBytes(WALRow miruActivityWALRow) throws Exception {
        return toBytes(miruActivityWALRow);
    }
}
