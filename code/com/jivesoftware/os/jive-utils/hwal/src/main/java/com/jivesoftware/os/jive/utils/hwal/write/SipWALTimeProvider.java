/*
 * Copyright 2014 Jive Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jivesoftware.os.jive.utils.hwal.write;

import com.jivesoftware.os.jive.utils.hwal.shared.api.SipWALTime;
import com.jivesoftware.os.jive.utils.ordered.id.TimestampProvider;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author jonathan
 */
public class SipWALTimeProvider {

    final private TimestampProvider timestampProvider;
    final private AtomicReference<TimeAndOrder> state;

    public SipWALTimeProvider(TimestampProvider timestampProvider) {
        this.timestampProvider = timestampProvider;
        this.state = new AtomicReference<>(new TimeAndOrder(timestampProvider.getTimestamp(), 0));
    }

    public SipWALTime currentSipTime() {
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

                    if (nextOrder > Integer.MAX_VALUE - 1) {
                        continue;
                    }

                    next = new TimeAndOrder(timestamp, nextOrder);
                } else {
                    next = new TimeAndOrder(timestamp, 0);
                }

                if (state.compareAndSet(current, next)) {
                    return new SipWALTime(next.time, next.order);
                }
            }
        }
    }

    private static final class TimeAndOrder {

        TimeAndOrder(long time, int order) {
            this.time = time;
            this.order = order;
        }
        final long time;
        final int order;
    }
}
