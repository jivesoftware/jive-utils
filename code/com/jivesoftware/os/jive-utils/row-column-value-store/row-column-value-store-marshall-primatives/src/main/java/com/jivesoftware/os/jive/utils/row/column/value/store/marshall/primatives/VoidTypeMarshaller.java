package com.jivesoftware.os.jive.utils.row.column.value.store.marshall.primatives;

import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.api.TypeMarshaller;

public class VoidTypeMarshaller implements TypeMarshaller<Void> {
    @Override
    public Void fromBytes(byte[] bytes) throws Exception {
        return null;
    }

    @Override
    public byte[] toBytes(Void aVoid) throws Exception {
        return new byte[0];
    }

    @Override
    public Void fromLexBytes(byte[] lexBytes) throws Exception {
        return null;
    }

    @Override
    public byte[] toLexBytes(Void aVoid) throws Exception {
        return new byte[0];
    }
}
