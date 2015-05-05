package com.jivesoftware.os.jive.utils.ordered.id;

public class ConstantWriterIdProvider implements WriterIdProvider {

    private final int writerId;

    public ConstantWriterIdProvider(int writerId) {
        this.writerId = writerId;
    }

    @Override
    public WriterId getWriterId() {
        return new ConstantWriterId(writerId);
    }
}
