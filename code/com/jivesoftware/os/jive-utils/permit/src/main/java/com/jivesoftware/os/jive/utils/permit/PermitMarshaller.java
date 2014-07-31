package com.jivesoftware.os.jive.utils.permit;

import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.api.TypeMarshaller;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class PermitMarshaller implements TypeMarshaller<Permit> {

    private final Charset utf_8 = Charset.forName("UTF-8");

    @Override
    public Permit fromBytes(byte[] bytes) throws Exception {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        int pool = bb.getInt();
        int id = bb.getInt();
        long issued = bb.getLong();
        int length = bb.getInt();
        byte[] ownerBytes = new byte[length];
        bb.get(ownerBytes);
        return new Permit(pool, id, issued, new String(ownerBytes, utf_8));
    }

    @Override
    public byte[] toBytes(Permit permit) throws Exception {
        byte[] ownerBytes = permit.owner.getBytes(utf_8);
        ByteBuffer buffer = ByteBuffer.allocate(4 + 4 + 8 + 4 + ownerBytes.length);

        buffer.putInt(permit.pool);
        buffer.putInt(permit.id);
        buffer.putLong(permit.issued);
        buffer.putInt(ownerBytes.length);
        buffer.put(ownerBytes);
        return buffer.array();
    }

    @Override
    public Permit fromLexBytes(byte[] bytes) throws Exception {
        return fromBytes(bytes);
    }

    @Override
    public byte[] toLexBytes(Permit permit) throws Exception {
        return toBytes(permit);
    }
}
