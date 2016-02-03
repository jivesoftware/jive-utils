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

    private int compute(byte[] a, int offset, int length) {
        int result = 1;
        for (int i = offset; i < offset + length; i++) {
            result = 31 * result + a[i];
        }
        return result;
    }
}
