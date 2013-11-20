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