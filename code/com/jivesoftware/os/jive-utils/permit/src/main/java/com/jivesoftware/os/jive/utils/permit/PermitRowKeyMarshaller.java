package com.jivesoftware.os.jive.utils.permit;

import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.api.TypeMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.primatives.IntArrayTypeMarshaller;

public class PermitRowKeyMarshaller implements TypeMarshaller<PermitRowKey> {
    IntArrayTypeMarshaller intArrayTypeMarshaller = new IntArrayTypeMarshaller();

    @Override
    public PermitRowKey fromBytes(byte[] bytes) throws Exception {
        int[] fields = intArrayTypeMarshaller.fromBytes(bytes);
        return new PermitRowKey(fields[0], fields[1]);
    }

    @Override
    public byte[] toBytes(PermitRowKey permitRowKey) throws Exception {
        return intArrayTypeMarshaller.toBytes(new int[] { permitRowKey.pool, permitRowKey.id });
    }

    @Override
    public PermitRowKey fromLexBytes(byte[] lexBytes) throws Exception {
        int[] fields = intArrayTypeMarshaller.fromLexBytes(lexBytes);
        return new PermitRowKey(fields[0], fields[1]);
    }

    @Override
    public byte[] toLexBytes(PermitRowKey permitRowKey) throws Exception {
        return intArrayTypeMarshaller.toLexBytes(new int[] { permitRowKey.pool, permitRowKey.id });
    }
}
