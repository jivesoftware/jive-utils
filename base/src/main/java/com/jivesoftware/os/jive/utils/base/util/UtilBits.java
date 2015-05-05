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
package com.jivesoftware.os.jive.utils.base.util;

/**
 *
 * @author jonathan
 */
public class UtilBits {

    final static char[] digits = {
        '0', '1'
    };

    public static String toBitString(long i) {
        int shift = 1;
        char[] buf = new char[64];
        int charPos = 64;
        int radix = 1 << shift;
        int mask = radix - 1;
        do {
            buf[--charPos] = digits[(int) (i & mask)];
            i >>>= shift;
        } while (charPos != 0);
        return new String(buf, charPos, (64 - charPos));
    }

    public static String toBitString(double i) {
        return toBitString(Double.doubleToLongBits(i));
    }

    public static String toBitString(byte[] bytes) {
        int shy = bytes.length % 8;
        if (shy != 0) {
            byte[] padded = new byte[bytes.length + (8 - shy)];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        long[] longs = bytesLongs(bytes);
        StringBuilder sb = new StringBuilder();
        for (long l : longs) {
            sb.append(toBitString(l));
        }
        if (shy == 0) {
            return sb.toString();
        } else {
            String s = sb.toString();
            return s.substring(0, s.length() - ((8 - shy) * 8));
        }
    }

    public static long[] bytesLongs(byte[] _bytes) {
        if (_bytes == null || _bytes.length == 0) {
            return null;
        }
        int longsCount = _bytes.length / 8;
        long[] longs = new long[longsCount];
        for (int i = 0; i < longsCount; i++) {
            longs[i] = bytesLong(_bytes, i * 8);
        }
        return longs;
    }

    public static long bytesLong(byte[] bytes, int _offset) {
        if (bytes == null) {
            return 0;
        }
        long v = 0;
        v |= (bytes[_offset + 0] & 0xFF);
        v <<= 8;
        v |= (bytes[_offset + 1] & 0xFF);
        v <<= 8;
        v |= (bytes[_offset + 2] & 0xFF);
        v <<= 8;
        v |= (bytes[_offset + 3] & 0xFF);
        v <<= 8;
        v |= (bytes[_offset + 4] & 0xFF);
        v <<= 8;
        v |= (bytes[_offset + 5] & 0xFF);
        v <<= 8;
        v |= (bytes[_offset + 6] & 0xFF);
        v <<= 8;
        v |= (bytes[_offset + 7] & 0xFF);
        return v;
    }
}
