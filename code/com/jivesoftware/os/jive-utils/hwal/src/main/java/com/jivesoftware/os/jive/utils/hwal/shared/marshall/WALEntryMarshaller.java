package com.jivesoftware.os.jive.utils.hwal.shared.marshall;

import com.jivesoftware.os.jive.utils.hwal.shared.api.WALEntry;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.api.TypeMarshaller;
import java.nio.ByteBuffer;

public class WALEntryMarshaller implements TypeMarshaller<WALEntry> {

    @Override
    public WALEntry fromBytes(byte[] bytes) throws Exception {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        return null;
    }

    @Override
    public byte[] toBytes(WALEntry instance) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(8);

        return buffer.array();
    }

    @Override
    public WALEntry fromLexBytes(byte[] bytes) throws Exception {
        return fromBytes(bytes);
    }

    @Override
    public byte[] toLexBytes(WALEntry instance) throws Exception {
        return toBytes(instance);
    }
}
