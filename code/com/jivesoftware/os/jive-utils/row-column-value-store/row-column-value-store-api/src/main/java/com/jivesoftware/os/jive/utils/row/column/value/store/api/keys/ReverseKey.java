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
package com.jivesoftware.os.jive.utils.row.column.value.store.api.keys;

public class ReverseKey {

    public static long reverseLongBytes(long toReverse) {
        int bytesInALong = 8;
        int bitsInAByte = 8;
        long fullByteMask = 256 - 1;

        long reversed = 0;

        for (int i = 0; i < bytesInALong; i++) {
            long temp = toReverse & (fullByteMask << i * bitsInAByte);

            if (i < bytesInALong / 2) {
                int shiftAmount = ((bytesInALong - 1) - 2 * i);
                temp = temp << (shiftAmount * bitsInAByte);
            } else {
                int shiftAmount = i - ((bytesInALong - 1) - i);
                temp = temp >>> (shiftAmount * bitsInAByte);
            }

            reversed |= temp;
        }
        return reversed;
    }
}
