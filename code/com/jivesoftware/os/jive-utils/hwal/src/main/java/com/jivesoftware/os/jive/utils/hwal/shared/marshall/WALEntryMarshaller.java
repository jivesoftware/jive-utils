package com.jivesoftware.os.jive.utils.hwal.shared.marshall;

import com.jivesoftware.os.jive.utils.hwal.shared.api.SipWALEntry;
import com.jivesoftware.os.jive.utils.hwal.shared.api.WALEntry;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.api.TypeMarshaller;
import java.nio.ByteBuffer;

public class WALEntryMarshaller implements TypeMarshaller<WALEntry> {

    @Override
    public WALEntry fromBytes(byte[] bytes) throws Exception {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long uniqueOrderingId = bb.getLong();
        long ingressedTimestampMillis = bb.getLong();
        int length = bb.getInt();
        byte[] key = new byte[length];
        bb.get(key);
        SipWALEntry sipWALEntry = new SipWALEntry(uniqueOrderingId, ingressedTimestampMillis, bytes);
        length = bb.getInt();
        byte[] payload = new byte[length];
        bb.get(payload);
        return new WALEntry(sipWALEntry, payload);
    }

    @Override
    public byte[] toBytes(WALEntry instance) throws Exception {
        SipWALEntry sipWALEntry = instance.getSipWALEntry();
        ByteBuffer buffer = ByteBuffer.allocate(8 + 8 + 4 + sipWALEntry.key.length + 4 + instance.payload.length);
        buffer.putLong(sipWALEntry.uniqueOrderingId);
        buffer.putLong(sipWALEntry.ingressedTimestampMillis);
        buffer.putInt(sipWALEntry.key.length);
        buffer.put(sipWALEntry.key);
        buffer.putInt(instance.payload.length);
        buffer.put(instance.payload);
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
