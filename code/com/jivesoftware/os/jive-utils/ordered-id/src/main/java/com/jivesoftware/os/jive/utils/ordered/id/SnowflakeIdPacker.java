package com.jivesoftware.os.jive.utils.ordered.id;

/**
 * Uses 42bits for time, 9bits for writerId, 12bits for orderId and 1 bit for add vs removed.
 */
public class SnowflakeIdPacker implements IdPacker {

    @Override
    public int bitsPrecisionOfOrderId() {
        return 12;
    }

    @Override
    public int bitsPrecisionOfTimestamp() {
        return 42;
    }

    @Override
    public int bitsPrecisionOfWriterId() {
        return 9;
    }

    @Override
    public long pack(long timestamp, int writerId, int orderId) {
        long id = (timestamp & 0x1FFFFFFFFFFL) << 9 + 12 + 1;
        id |= ((writerId & 0x1FF) << 12 + 1);
        id |= ((orderId & 0xFFF) << 1);
        return id;
    }

    @Override
    public long[] unpack(long packedId) {
        long packed = packedId;
        long time = (packed & (0x1FFFFFFFFFFL << 9 + 12 + 1)) >>> 9 + 12 + 1;
        int writer = (int) ((packed & (0x1FF << 12 + 1)) >>> 12 + 1);
        int order = (int) ((packed & (0xFFF << 1)) >>> 1);
        return new long[]{time, writer, order};
    }

}
