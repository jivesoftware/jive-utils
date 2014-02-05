package com.jivesoftware.os.jive.utils.map.store.pages;

/**
 *
 * @author jonathan
 */
public class ByteArrayPage implements Page {

    /**
     *
     */
    protected byte[] array;

    /**
     *
     * @param _array
     */
    public ByteArrayPage(byte[] _array) {
        array = _array;
    }

    /**
     *
     */
    @Override
    public void init() {
    }

    /**
     *
     * @return
     */
    @Override
    final public Object raw() {
        return array;
    }

    /**
     *
     * @return
     */
    @Override
    final public long size() {
        return array.length;
    }

    /**
     *
     * @param pageStart
     * @return
     */
    @Override
    final public byte read(int pageStart) {
        return array[pageStart];
    }

    /**
     *
     * @param pageStart
     * @param v
     */
    @Override
    final public void write(int pageStart, byte v) {
        array[pageStart] = v;
    }

    /**
     *
     * @param pageStart
     * @return
     */
    @Override
    final public int readInt(int pageStart) {
        int v = 0;
        v |= (array[pageStart + 0] & 0xFF);
        v <<= 8;
        v |= (array[pageStart + 1] & 0xFF);
        v <<= 8;
        v |= (array[pageStart + 2] & 0xFF);
        v <<= 8;
        v |= (array[pageStart + 3] & 0xFF);
        return v;
    }

    /**
     *
     * @param pageStart
     * @return
     */
    @Override
    final public float readFloat(int pageStart) {
        int v = 0;
        v |= (array[pageStart + 0] & 0xFF);
        v <<= 8;
        v |= (array[pageStart + 1] & 0xFF);
        v <<= 8;
        v |= (array[pageStart + 2] & 0xFF);
        v <<= 8;
        v |= (array[pageStart + 3] & 0xFF);
        return Float.intBitsToFloat(v);
    }

    /**
     *
     * @param pageStart
     * @return
     */
    @Override
    final public long readLong(int pageStart) {
        long v = 0;
        v |= (array[pageStart + 0] & 0xFF);
        v <<= 8;
        v |= (array[pageStart + 1] & 0xFF);
        v <<= 8;
        v |= (array[pageStart + 2] & 0xFF);
        v <<= 8;
        v |= (array[pageStart + 3] & 0xFF);
        v <<= 8;
        v |= (array[pageStart + 4] & 0xFF);
        v <<= 8;
        v |= (array[pageStart + 5] & 0xFF);
        v <<= 8;
        v |= (array[pageStart + 6] & 0xFF);
        v <<= 8;
        v |= (array[pageStart + 7] & 0xFF);
        return v;
    }

    /**
     *
     * @param pageStart
     * @return
     */
    @Override
    final public double readDouble(int pageStart) {
        long v = 0;
        v |= (array[pageStart + 0] & 0xFF);
        v <<= 8;
        v |= (array[pageStart + 1] & 0xFF);
        v <<= 8;
        v |= (array[pageStart + 2] & 0xFF);
        v <<= 8;
        v |= (array[pageStart + 3] & 0xFF);
        v <<= 8;
        v |= (array[pageStart + 4] & 0xFF);
        v <<= 8;
        v |= (array[pageStart + 5] & 0xFF);
        v <<= 8;
        v |= (array[pageStart + 6] & 0xFF);
        v <<= 8;
        v |= (array[pageStart + 7] & 0xFF);
        return Double.longBitsToDouble(v);
    }

    /**
     *
     * @param pageStart
     * @param v
     */
    @Override
    final public void writeInt(int pageStart, int v) {
        array[pageStart + 0] = (byte) (v >>> 24);
        array[pageStart + 1] = (byte) (v >>> 16);
        array[pageStart + 2] = (byte) (v >>> 8);
        array[pageStart + 3] = (byte) v;
    }

    /**
     *
     * @param pageStart
     * @param read
     * @param offset
     * @param length
     */
    @Override
    final public void read(int pageStart, byte[] read, int offset, int length) {
        System.arraycopy(array, pageStart, read, offset, length);
    }

    /**
     *
     * @param pageStart
     * @param towrite
     * @param offest
     * @param length
     */
    @Override
    final public void write(int pageStart, byte[] towrite, int offest, int length) {
        System.arraycopy(towrite, offest, array, pageStart, length);
    }

    /**
     *
     * @param pageStart
     * @param keySize
     * @param b
     * @param boffset
     * @return
     */
    @Override
    final public boolean equals(long pageStart, int keySize, byte[] b, int boffset) {
        for (int i = 0; i < keySize; i++) {
            if (array[(int) (pageStart + 1 + i)] != b[boffset + i]) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isLoaded() {
        return true;
    }

    /**
     *
     */
    @Override
    final public void force() {

    }
}
