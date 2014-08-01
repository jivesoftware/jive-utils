package com.jivesoftware.os.jive.utils.permit;

import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.api.TypeMarshaller;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class PermitRowKeyMarshaller implements TypeMarshaller<PermitRowKey> {
    private final Charset utf_8 = Charset.forName("UTF-8");

    @Override
    public PermitRowKey fromBytes(byte[] bytes) throws Exception {

        ByteBuffer bb = ByteBuffer.wrap(bytes);
        int id = bb.getInt();

        int length = bb.getInt();
        byte[] poolBytes = new byte[length];
        bb.get(poolBytes);

        return new PermitRowKey(new String(poolBytes, utf_8), id);
    }

    @Override
    public byte[] toBytes(PermitRowKey key) throws Exception {
        byte[] poolBytes = key.pool.getBytes(utf_8);
        ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + poolBytes.length);

        buffer.putInt(key.id);
        buffer.putInt(poolBytes.length);
        buffer.put(poolBytes);
        return buffer.array();
    }


    @Override
    public PermitRowKey fromLexBytes(byte[] lexBytes) throws Exception {
        return fromBytes(lexBytes);
    }

    @Override
    public byte[] toLexBytes(PermitRowKey permitRowKey) throws Exception {
        return toBytes(permitRowKey);
    }
}
