package com.jivesoftware.os.jive.utils.hwal.shared.marshall;

import com.jivesoftware.os.jive.utils.hwal.shared.api.SipWALEntry;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.api.TypeMarshaller;
import java.nio.ByteBuffer;

public class SipWALEntryMarshaller implements TypeMarshaller<SipWALEntry> {

    @Override
    public SipWALEntry fromBytes(byte[] bytes) throws Exception {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long uniqueOrderingId = bb.getLong();
        long ingressedTimestampMillis = bb.getLong();
        int length = bb.getInt();
        byte[] rawKey = new byte[length];
        bb.get(rawKey);
        return new SipWALEntry(uniqueOrderingId, ingressedTimestampMillis, bytes);
    }

    @Override
    public byte[] toBytes(SipWALEntry instance) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(8 + 8 + 4 + instance.key.length);
        buffer.putLong(instance.uniqueOrderingId);
        buffer.putLong(instance.ingressedTimestampMillis);
        buffer.putInt(instance.key.length);
        buffer.put(instance.key);
        return buffer.array();
    }

    @Override
    public SipWALEntry fromLexBytes(byte[] bytes) throws Exception {
        return fromBytes(bytes);
    }

    @Override
    public byte[] toLexBytes(SipWALEntry instance) throws Exception {
        return toBytes(instance);
    }
}
