package com.jivesoftware.os.jive.utils.ordered.id;

import java.util.concurrent.atomic.AtomicReference;

/**
 * An order id provider which generates ids using a combination of system time, a logical writer id, and an incrementing sequence number.
 */
public final /* hi mark */ class OrderIdProviderImpl implements OrderIdProvider {
    private final int maxOrderId;
    final private TimestampProvider timestampProvider;
    private final IdPacker idPacker;
    final private int writerId;
    final private AtomicReference<TimeAndOrder> state;

    public OrderIdProviderImpl(int writerId) {
        this(writerId, new SnowflakeIdPacker(), new JiveEpochTimestampProvider());
    }

    public OrderIdProviderImpl(int writerId, IdPacker idPacker, TimestampProvider timestampProvider) {
        int maxWritterId = (int) Math.pow(2, idPacker.bitsPrecisionOfWriterId()) - 1;
        if (writerId < 0 || writerId > maxWritterId) {
            throw new IllegalArgumentException("writerId is out of range must be 0.." + maxWritterId);
        }
        this.maxOrderId = (int) Math.pow(2, idPacker.bitsPrecisionOfOrderId()) - 1;
        this.writerId = writerId;
        this.idPacker = idPacker;
        this.timestampProvider = timestampProvider;
        this.state = new AtomicReference<>(new TimeAndOrder(timestampProvider.getTimestamp(), 0));
    }

    @Override
    public long nextId() throws IdGenerationException {
        while (true) { // exits on successful compare-and-set
            long timestamp = timestampProvider.getTimestamp();
            TimeAndOrder current = state.get();

            if (current.time > timestamp) {
                long retryWaitHint = current.time - timestamp;

                throw new IdGenerationException(retryWaitHint,
                    String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
                    retryWaitHint));
            } else {
                TimeAndOrder next;

                if (current.time == timestamp) {
                    int nextOrder = current.order + 1;

                    if (nextOrder > maxOrderId) {
                        continue;
                    }

                    next = new TimeAndOrder(timestamp, nextOrder);
                } else {
                    next = new TimeAndOrder(timestamp, 0);
                }

                if (state.compareAndSet(current, next)) {
                    return idPacker.pack(next.time, writerId, next.order);
                }
            }
        }
    }

    /**
     *
     * @param id
     * @return
     */
    public long[] unpack(long id) {
        return idPacker.unpack(id);
    }

    //if you want to be annoying, you could avoid this object by packing the state in a long and test/set an AtomicLong
    private static final class TimeAndOrder {

        TimeAndOrder(long time, int order) {
            this.time = time;
            this.order = order;
        }
        final long time;
        final int order;
    }
}
