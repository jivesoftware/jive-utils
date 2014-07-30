package com.jivesoftware.os.jive.utils.hwal.shared.hbase;

import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.api.TypeMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.api.UtilLexMarshaller;
import java.nio.ByteBuffer;

public class WALColumnKeyMarshaller implements TypeMarshaller<WALColumnKey> {

    @Override
    public WALColumnKey fromBytes(byte[] bytes) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        byte sort = buffer.get();
        long collisionId = buffer.getLong();

        return new WALColumnKey(sort, collisionId);
    }

    @Override
    public byte[] toBytes(WALColumnKey miruActivityWALColumnKey) throws Exception {
        int capacity = 9; // sort (1 byte) + collisionId (8 bytes)

        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.put(miruActivityWALColumnKey.getSort());
        buffer.putLong(miruActivityWALColumnKey.getCollisionId());
        return buffer.array();
    }

    @Override
    public WALColumnKey fromLexBytes(byte[] bytes) throws Exception {
        byte sort = bytes[0];
        long collisionId = UtilLexMarshaller.longFromLex(bytes, 1);

        return new WALColumnKey(sort, collisionId);
    }

    @Override
    public byte[] toLexBytes(WALColumnKey miruActivityWALColumnKey) throws Exception {
        int capacity = 9; // sort (1 byte) + collisionId (8 bytes)

        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.put(miruActivityWALColumnKey.getSort());
        buffer.put(UtilLexMarshaller.longToLex(miruActivityWALColumnKey.getCollisionId()));

        return buffer.array();
    }
}
