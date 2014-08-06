package com.jivesoftware.os.jive.utils.id;


/**
 *
 */
public class SaltingImmutableByteArrayMarshaller
        extends SaltingDelegatingMarshaller<ImmutableByteArrayMarshaller, ImmutableByteArray> {

    public SaltingImmutableByteArrayMarshaller() {
        super(new ImmutableByteArrayMarshaller());
    }
}
