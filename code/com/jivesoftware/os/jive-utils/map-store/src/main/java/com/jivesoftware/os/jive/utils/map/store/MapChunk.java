package com.jivesoftware.os.jive.utils.map.store;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;

/**
 *
 * @author jonathan
 */
public class MapChunk {

    final MapStore mapStore;
    private final ByteBuffer array;
    int keySize; // read only
    int payloadSize; // read only
    int capacity; // read only
    int maxCount; // read only

    /**
     *
     * @param mapStore
     * @param array
     */
    public MapChunk(MapStore mapStore, ByteBuffer array) {
        this.mapStore = mapStore;
        this.array = array;
    }

    /**
     *
     */
    public void init() {
        keySize = mapStore.getKeySize(this);
        payloadSize = mapStore.getPayloadSize(this);
        maxCount = mapStore.getMaxCount(this);
        capacity = mapStore.getCapacity(this);
    }

    /**
     * @return
     */
    final public Object raw() {
        return array;
    }

    /**
     * @return
     */
    final public long size() {
        return array.capacity();
    }

    /**
     * @param pageStart
     * @return
     */
    final public byte read(int pageStart) {
        return array.get(pageStart);
    }

    /**
     * @param pageStart
     * @param v
     */
    final public void write(int pageStart, byte v) {
        array.put(pageStart, v);
    }

    /**
     * @param pageStart
     * @return
     */
    final public int readInt(int pageStart) {
        return array.getInt(pageStart);
    }

    /**
     * @param pageStart
     * @return
     */
    final public float readFloat(int pageStart) {
        return array.getFloat(pageStart);
    }

    /**
     * @param pageStart
     * @return
     */
    final public long readLong(int pageStart) {
        return array.getLong(pageStart);
    }

    /**
     * @param pageStart
     * @return
     */
    final public double readDouble(int pageStart) {
        return array.getDouble(pageStart);
    }

    /**
     * @param pageStart
     * @param v
     */
    final public void writeInt(int pageStart, int v) {
        array.putInt(pageStart, v);
    }

    /**
     * @param pageStart
     * @param read
     * @param offset
     * @param length
     */
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
    public boolean isLoaded() {
        if (array instanceof MappedByteBuffer) {
            return ((MappedByteBuffer) array).isLoaded();
        }
        return true;
    }


    /**
     *
     */
    final public void force() {
        if (array instanceof MappedByteBuffer) {
            ((MappedByteBuffer) array).force();
        }
    }
}
