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
