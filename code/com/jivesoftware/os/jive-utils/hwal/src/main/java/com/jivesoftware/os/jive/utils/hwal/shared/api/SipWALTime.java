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
package com.jivesoftware.os.jive.utils.hwal.shared.api;

/**
 *
 * @author jonathan
 */
public class SipWALTime implements Comparable<SipWALTime> {

    private final long timestamp;
    private final int order;

    public SipWALTime(long timestamp, int order) {
        this.timestamp = timestamp;
        this.order = order;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public String toString() {
        return "WALSipTime{" + "timestamp=" + timestamp + ", order=" + order + '}';
    }

    @Override
    public int compareTo(SipWALTime o) {
        int i = Long.compare(timestamp, o.timestamp);
        if (i == 0) {
            i = Integer.compare(order, o.order);
        }
        return i;
    }

}
