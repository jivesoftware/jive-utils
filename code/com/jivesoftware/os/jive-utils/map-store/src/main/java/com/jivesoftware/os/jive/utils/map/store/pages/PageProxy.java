package com.jivesoftware.os.jive.utils.map.store.pages;

/**
 *
 * @author jonathan
 */
public class PageProxy implements Page {

    Page page;

    /**
     *
     * @param _page
     */
    public PageProxy(Page _page) {
        page = _page;
    }

    /**
     *
     */
    @Override
    public void init() {
        page.init();
    }

    /**
     *
     * @return
     */
    public Page backingPage() {
        return page;
    }

    /**
     *
     * @return
     */
    @Override
    public Object raw() {
        return page.raw();
    }

    /**
     *
     * @return
     */
    @Override
    public long size() {
        return page.size();
    }

    /**
     *
     * @param pageStart
     * @return
     */
    @Override
    public byte read(int pageStart) {
        return page.read(pageStart);
    }

    /**
     *
     * @param pageStart
     * @param v
     */
    @Override
    public void write(int pageStart, byte v) {
        page.write(pageStart, v);
    }

    /**
     *
     * @param pageStart
     * @return
     */
    @Override
    public int readInt(int pageStart) {
        return page.readInt(pageStart);
    }

    /**
     *
     * @param pageStart
     * @return
     */
    @Override
    public float readFloat(int pageStart) {
        return page.readFloat(pageStart);
    }

    /**
     *
     * @param pageStart
     * @return
     */
    @Override
    public long readLong(int pageStart) {
        return page.readLong(pageStart);
    }

    /**
     *
     * @param pageStart
     * @return
     */
    @Override
    public double readDouble(int pageStart) {
        return page.readDouble(pageStart);
    }

    /**
     *
     * @param pageStart
     * @param v
     */
    @Override
    public void writeInt(int pageStart, int v) {
        page.writeInt(pageStart, v);
    }

    /**
     *
     * @param pageStart
     * @param read
     * @param offset
     * @param length
     */
    @Override
    public void read(int pageStart, byte[] read, int offset, int length) {
        page.read(pageStart, read, offset, length);
    }

    /**
     *
     * @param pageStart
     * @param towrite
     * @param offest
     * @param length
     */
    @Override
    public void write(int pageStart, byte[] towrite, int offest, int length) {
        page.write(pageStart, towrite, offest, length);
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
    public boolean equals(long pageStart, int keySize, byte[] b, int boffset) {
        return page.equals(pageStart, keySize, b, boffset);
    }

    /**
     *
     * @return
     */
    @Override
    final public boolean isLoaded() {
        return page.isLoaded();
    }

    /**
     *
     */
    @Override
    final public void force() {
        page.force();
    }
}
