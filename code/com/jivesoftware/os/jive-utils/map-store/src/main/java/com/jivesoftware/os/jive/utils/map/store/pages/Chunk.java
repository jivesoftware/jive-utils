package com.jivesoftware.os.jive.utils.map.store.pages;

/**
 *
 * @author jonathan
 */
public interface Chunk {

    void init();

    Object raw();

    long size();

    byte read(int pageStart);

    void write(int pageStart, byte v);

    long readLong(int pageStart);

    double readDouble(int pageStart);

    int readInt(int pageStart);

    float readFloat(int pageStart);

    void writeInt(int pageStart, int v);

    void read(int pageStart, byte[] read, int offset, int length);

    void write(int pageStart, byte[] towrite, int offest, int length);

    boolean equals(long pageStart, int keySize, byte[] b, int boffset);

    boolean isLoaded();

    void force();
}
