package com.jivesoftware.os.jive.utils.collections.bah;

/**
 *
 * @author jonathan.colt
 */
public class BAHasher {

    public static final BAHasher SINGLETON = new BAHasher();

    public int hashCode(byte[] bytes, int offset, int length) {
        return bytes == null ? 0 : compute(bytes, offset, length);
    }

    private int compute(byte[] bytes, int offset, int length) {
        int hash = 0;
        long randMult = 0x5_DEEC_E66DL;
        long randAdd = 0xBL;
        long randMask = (1L << 48) - 1;
        long seed = bytes.length;
        for (int i = offset; i < offset + length; i++) {
            long x = (seed * randMult + randAdd) & randMask;

            seed = x;
            hash += (bytes[i] + 128) * x;
        }
        return hash;
    }
}
