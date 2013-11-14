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
