/*
 * Copyright 2013 Jive Software, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.jivesoftware.os.jive.utils.ordered.id;

import java.util.concurrent.atomic.AtomicReference;

/**
 * An order id provider which generates ids using a combination of system time, a logical writer id, and an incrementing sequence number.
 */
public final /* hi mark */ class OrderIdProviderImpl implements TimestampedOrderIdProvider {

    private final int maxOrderId;
    final private TimestampProvider timestampProvider;
    private final IdPacker idPacker;
    final private WriterIdProvider writerIdProvider;
    final private AtomicReference<TimeAndOrder> state;

    public OrderIdProviderImpl(WriterIdProvider writerIdProvider) {
        this(writerIdProvider, new SnowflakeIdPacker(), new JiveEpochTimestampProvider());
    }

    public OrderIdProviderImpl(WriterIdProvider writerIdProvider, IdPacker idPacker, TimestampProvider timestampProvider) {
        this.maxOrderId = (int) Math.pow(2, idPacker.bitsPrecisionOfOrderId()) - 1;
        this.writerIdProvider = writerIdProvider;
        this.idPacker = idPacker;
        this.timestampProvider = timestampProvider;
        this.state = new AtomicReference<>(new TimeAndOrder(timestampProvider.getTimestamp(), 0));
    }

    @Override
    public long getApproximateId(long currentTimeMillis) {
        WriterId writerId = getWriterId();
        long approximateTimestamp = timestampProvider.getApproximateTimestamp(currentTimeMillis);
        return idPacker.pack(approximateTimestamp, writerId.getId(), 0);
    }

    @Override
    public long getApproximateId(long id, long wallClockDeltaMillis) {
        long[] unpacked = idPacker.unpack(id);
        return idPacker.pack(timestampProvider.getApproximateTimestamp(unpacked[0], wallClockDeltaMillis), (int) unpacked[1], (int) unpacked[2]);
    }

    @Override
    public long nextId() {
        while (true) { // exits on successful compare-and-set
            long timestamp = timestampProvider.getTimestamp();
            TimeAndOrder current = state.get();

            if (current.time > timestamp) {
                long retryWaitHint = current.time - timestamp;

                try {
                    Thread.sleep(retryWaitHint);
                } catch (InterruptedException ie) {
                    Thread.interrupted();
                }

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
                    WriterId writerId;
                    long id;
                    do {
                        writerId = getWriterId();
                        id = idPacker.pack(next.time, writerId.getId(), next.order);
                    } while (!writerId.isValid());
                    return id;
                }
            }
        }
    }

    private WriterId getWriterId() {
        WriterId writerId;
        try {
            writerId = writerIdProvider.getWriterId();
        } catch (OutOfWriterIdsException e) {
            throw new RuntimeException("Ran out of writer IDs. This should never happen. Failing fast.", e);
        }

        int maxWritterId = (int) Math.pow(2, idPacker.bitsPrecisionOfWriterId()) - 1;
        if (writerId.getId() < 0 || writerId.getId() > maxWritterId) {
            throw new IllegalArgumentException("writerId is out of range must be 0.." + maxWritterId);
        }

        return writerId;
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
