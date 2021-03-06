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

public class JiveEpochTimestampProvider implements TimestampProvider {

    public static final long JIVE_EPOCH = 1349734204785L; // Mon Oct 8, 1012 EOA epoch

    @Override
    public long getTimestamp() {
        return getApproximateTimestamp(System.currentTimeMillis());
    }

    @Override
    public long getApproximateTimestamp(long currentTimeMillis) {
        return currentTimeMillis - JIVE_EPOCH;
    }

    @Override
    public long getApproximateTimestamp(long internalTimestamp, long wallClockDeltaMillis) {
        return internalTimestamp - wallClockDeltaMillis;
    }

}
