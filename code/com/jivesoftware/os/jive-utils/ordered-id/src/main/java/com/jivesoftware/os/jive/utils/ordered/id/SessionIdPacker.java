package com.jivesoftware.os.jive.utils.ordered.id;

/**
 * Uses 38bits for time, 14bits for writerId, 12bits for orderId
 */
public class SessionIdPacker implements IdPacker {

    @Override
    public long pack(long timestamp, int writerId, int orderId) {
        long id = (timestamp & 0x1FFFFFFFFFL) << 14 + 12;
        id |= ((writerId & 0x3FFF) << 12);
        id |= ((orderId & 0xFFF));
        return id;
    }

    @Override
    public long[] unpack(long packedId) {
        long packed = packedId;
        long time = (packed & (0x1FFFFFFFFFL << 14 + 12)) >>> 14 + 12;
        int writer = (int) ((packed & (0x3FFF << 12)) >>> 12);
        int order = (int) packed & 0xFFF;
        return new long[]{time, writer, order};
    }

    @Override
    public int bitsPrecisionOfOrderId() {
        return 12;
    }

    @Override
    public int bitsPrecisionOfTimestamp() {
        return 38;
    }

    @Override
    public int bitsPrecisionOfWriterId() {
        return 14;
    }

}
