package com.jivesoftware.os.jive.utils.ordered.id;

public interface IdPacker {

    /**
     * Number of bits available for time in millis.
     * @return
     */
    int bitsPrecisionOfTimestamp();

    /**
     * Number of bits available for writer ID's.
     * @return
     */
    int bitsPrecisionOfWriterId();

    /**
     * Number of bits available for order ID's.
     * @return
     */
    int bitsPrecisionOfOrderId();

    /**
     * Packs these three values into a long
     *
     * @param timestamp
     * @param writerId
     * @param orderId
     * @return
     */
    long pack(long timestamp, int writerId, int orderId);

    /**
     * Unpacks long into the following form new long[]{time, writer, order}
     *
     * @param packedId
     * @return new long[]{time, writer, order}
     */
    long[] unpack(long packedId);

}
