package com.jivesoftware.os.jive.utils.hwal.shared.marshall;

import com.jivesoftware.os.jive.utils.hwal.shared.api.SipWALTime;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.api.TypeMarshaller;
import java.nio.ByteBuffer;

public class SipWALTimeMarshaller implements TypeMarshaller<SipWALTime> {

    @Override
    public SipWALTime fromBytes(byte[] bytes) throws Exception {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long timestamp = bb.getLong();
        int order = bb.getInt();
        return new SipWALTime(timestamp, order);
    }

    @Override
    public byte[] toBytes(SipWALTime instance) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(8 + 4);
        buffer.putLong(instance.getTimestamp());
        buffer.putInt(instance.getOrder());
        return buffer.array();
    }


    @Override
    public SipWALTime fromLexBytes(byte[] bytes) throws Exception {
        return fromBytes(bytes);
    }

    @Override
    public byte[] toLexBytes(SipWALTime instance) throws Exception {
        return toBytes(instance);
    }
}
