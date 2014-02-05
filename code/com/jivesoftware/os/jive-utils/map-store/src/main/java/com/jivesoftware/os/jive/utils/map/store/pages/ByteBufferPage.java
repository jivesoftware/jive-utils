package com.jivesoftware.os.jive.utils.map.store.pages;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

/**
 * @author jonathan
 */
public class ByteBufferPage implements Page {

    /**
     *
     */
    protected ByteBuffer array;

    /**
     * @param _array
     */
    public ByteBufferPage(ByteBuffer _array) {
        array = _array;
    }

    /**
     *
     */
    @Override
    public void init() {
    }

    /**
     * @return
     */
    @Override
    final public Object raw() {
        return array;
    }

    /**
     * @return
     */
    @Override
    final public long size() {
        return array.capacity();
    }

    /**
     * @param pageStart
     * @return
     */
    @Override
    final public byte read(int pageStart) {
        return array.get(pageStart);
    }

    /**
     * @param pageStart
     * @param v
     */
    @Override
    final public void write(int pageStart, byte v) {
        array.put(pageStart, v);
    }

    /**
     * @param pageStart
     * @return
     */
    @Override
    final public int readInt(int pageStart) {
        return array.getInt(pageStart);
    }

    /**
     * @param pageStart
     * @return
     */
    @Override
    final public float readFloat(int pageStart) {
        return array.getFloat(pageStart);
    }

    /**
     * @param pageStart
     * @return
     */
    @Override
    final public long readLong(int pageStart) {
        return array.getLong(pageStart);
    }

    /**
     * @param pageStart
     * @return
     */
    @Override
    final public double readDouble(int pageStart) {
        return array.getDouble(pageStart);
    }

    /**
     * @param pageStart
     * @param v
     */
    @Override
    final public void writeInt(int pageStart, int v) {
        array.putInt(pageStart, v);
    }

    /**
     * @param pageStart
     * @param read
     * @param offset
     * @param length
     */
    @Override
    final public void read(int pageStart, byte[] read, int offset, int length) {
        array.position(pageStart);
        array.get(read, offset, length);
    }

    /**
     * @param pageStart
     * @param towrite
     * @param offest
     * @param length
     */
    @Override
    final public void write(int pageStart, byte[] towrite, int offest, int length) {
        array.position(pageStart);
        array.put(towrite, offest, length);
    }

    /**
     * @param pageStart
     * @param keySize
     * @param b
     * @param boffset
     * @return
     */
    @Override
    final public boolean equals(long pageStart, int keySize, byte[] b, int boffset) {
        for (int i = 0; i < keySize; i++) {
            int pageIndex = (int) (pageStart + 1 + i);
            if (array.get(pageIndex) != b[boffset + i]) {
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
        if (array instanceof MappedByteBuffer) {
            return ((MappedByteBuffer) array).isLoaded();
        }
        return true;
    }

    /**
     *
     */
    @Override
    final public void force() {
        if (array instanceof MappedByteBuffer) {
            ((MappedByteBuffer) array).force();
        }
    }
}
