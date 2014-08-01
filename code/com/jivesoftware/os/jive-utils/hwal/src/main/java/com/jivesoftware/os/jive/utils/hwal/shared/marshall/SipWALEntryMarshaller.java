package com.jivesoftware.os.jive.utils.hwal.shared.marshall;

import com.jivesoftware.os.jive.utils.hwal.shared.api.SipWALEntry;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.api.TypeMarshaller;
import java.nio.ByteBuffer;

public class SipWALEntryMarshaller implements TypeMarshaller<SipWALEntry> {

    @Override
    public SipWALEntry fromBytes(byte[] bytes) throws Exception {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        return null;
    }

    @Override
    public byte[] toBytes(SipWALEntry instance) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(8);

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
