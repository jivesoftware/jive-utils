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
package com.jivesoftware.os.jive.utils.row.column.value.store.marshall.api;

import java.nio.charset.Charset;

/**
 * Helper methods that marshalls java primatives to and from lexicographical sortables
 * http://brunodumon.wordpress.com/2010/02/17/building-indexes-using-hbase-mapping-strings-numbers-and-dates-onto-bytes/
 *
 * @author jonathan
 */
public class UtilLexMarshaller {

    static private Charset UTF8 = Charset.forName("UTF8");

    static private final int c32 = 0x80000000;
    static private final long c64 = 0x8000000000000000L;

    private UtilLexMarshaller() {
    }

    /**
     * Flips all the bits in the byte array
     *
     * @param v
     * @return
     */
    public static byte[] invert(byte[] v) {
        return invert(v, 0, v.length);
    }

    public static byte[] invert(byte[] v, int offset, int length) {
        for (int i = offset; i < length; i++) {
            v[i] ^= 0xFF;
        }
        return v;
    }

    /**
     * Converts a regular java long to a lexicographical sortable long
     *
     * @param v
     * @return
     */
    public static byte[] stringToLex(String v) {
        if (v == null) {
            return new byte[0];
        }
        byte[] bytes = v.getBytes(UTF8);
        return bytes;
    }

    /**
     * Converts lexicographical sortable long to a regular java long
     *
     * @return
     */
    public static String stringFromLex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return stringFromLex(bytes, 0, bytes.length);
    }

    /**
     * Converts lexicographical sortable long to a regular java long
     *
     * @return
     */
    public static String stringFromLex(byte[] bytes, int offset, int length) {
        if (bytes == null) {
            return null;
        }
        if (bytes.length - offset == 0) {
            return null;
        }
        return new String(bytes, offset, length, UTF8);
    }

    /**
     * Converts a regular java long to a lexicographical sortable long
     *
     * @param v
     * @return
     */
    public static byte[] longToLex(long v) {
        v ^= c64;
        return longBytes(v, new byte[8], 0);
    }

    /**
     * Converts lexicographical sortable long to a regular java long
     *
     * @return
     */
    public static long longFromLex(byte[] bytes) {
        long v = bytesLong(bytes, 0);
        v ^= c64;
        return v;
    }

    /**
     * Converts lexicographical sortable long to a regular java long
     *
     * @return
     */
    public static long longFromLex(byte[] bytes, int offset) {
        long v = bytesLong(bytes, offset);
        v ^= c64;
        return v;
    }

    /**
     * Converts a regular java int to a lexicographical sortable long
     *
     * @param v
     * @return
     */
    public static byte[] intToLex(int v) {
        v ^= c32;
        return intBytes(v, new byte[4], 0);
    }

    /**
     * Converts lexicographical sortable int to a regular java int
     *
     * @return
     */
    public static int intFromLex(byte[] bytes) {
        int v = bytesInt(bytes, 0);
        v ^= c32;
        return v;
    }

    /**
     * Converts lexicographical sortable int to a regular java int
     *
     * @return
     */
    public static int intFromLex(byte[] bytes, int offest) {
        int v = bytesInt(bytes, offest);
        v ^= c32;
        return v;
    }

    /**
     * Converts a regular java double to a lexicographical sortable double
     *
     * @param v
     * @return
     */
    public static byte[] doubleToLex(double v) {
        long l = Double.doubleToLongBits(v);
        if (Double.isNaN(v)) { // == on double in this case is ok
        } else if (v >= 0) {
            l ^= c64;
        } else {
            l ^= 0xFFFFFFFFFFFFFFFFL;
        }
        return longBytes(l, new byte[8], 0);
    }

    /**
     * Converts lexicographical sortable double to a regular java double
     *
     * @param v
     * @return
     */
    public static double doubleFromLex(byte[] v) {
        long l = bytesLong(v, 0);
        if (l == Double.doubleToLongBits(Double.NaN)) { // == on double in this case is ok
        } else if (l > 0) {
            l ^= 0xFFFFFFFFFFFFFFFFL;
        } else {
            l ^= c64;
        }
        return Double.longBitsToDouble(l);
    }

    /**
     *
     * @param v
     * @param _bytes
     * @param _offset
     * @return
     */
    public static byte[] longBytes(long v, byte[] _bytes, int _offset) {
        _bytes[_offset + 0] = (byte) (v >>> 56);
        _bytes[_offset + 1] = (byte) (v >>> 48);
        _bytes[_offset + 2] = (byte) (v >>> 40);
        _bytes[_offset + 3] = (byte) (v >>> 32);
        _bytes[_offset + 4] = (byte) (v >>> 24);
        _bytes[_offset + 5] = (byte) (v >>> 16);
        _bytes[_offset + 6] = (byte) (v >>> 8);
        _bytes[_offset + 7] = (byte) v;
        return _bytes;
    }

    /**
     *
     * @param bytes
     * @param _offset
     * @return
     */
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

    /**
     *
     * @param v
     * @param _bytes
     * @param _offset
     * @return
     */
    public static byte[] intBytes(int v, byte[] _bytes, int _offset) {
        _bytes[_offset + 0] = (byte) (v >>> 24);
        _bytes[_offset + 1] = (byte) (v >>> 16);
        _bytes[_offset + 2] = (byte) (v >>> 8);
        _bytes[_offset + 3] = (byte) v;
        return _bytes;
    }

    public static int bytesInt(byte[] bytes, int _offset) {
        int v = 0;
        v |= (bytes[_offset + 0] & 0xFF);
        v <<= 8;
        v |= (bytes[_offset + 1] & 0xFF);
        v <<= 8;
        v |= (bytes[_offset + 2] & 0xFF);
        v <<= 8;
        v |= (bytes[_offset + 3] & 0xFF);
        return v;
    }
}
