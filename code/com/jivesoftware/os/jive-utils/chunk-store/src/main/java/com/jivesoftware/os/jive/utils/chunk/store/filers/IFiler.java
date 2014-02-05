
package com.jivesoftware.os.jive.utils.chunk.store.filers;

import java.io.IOException;

/**
 *
 * @author Administrator
 */
public interface IFiler extends IReadable, IWriteable {

    /**
     *
     */
    final public static String cRead = "r";
    /**
     *
     */
    final public static String cWrite = "rw";
    /**
     *
     */
    final public static String cReadWrite = "rw";

    /**
     *
     * @return
     */
    public Object lock();

    /**
     *
     * @param position
     * @throws IOException
     */
    public void seek(long position) throws IOException;

    /**
     *
     * @param position
     * @return
     * @throws IOException
     */
    public long skip(long position) throws IOException;

    /**
     *
     * @return
     * @throws IOException
     */
    public long length() throws IOException;

    /**
     *
     * @param len
     * @throws IOException
     */
    public void setLength(long len) throws IOException;

    /**
     *
     * @return
     * @throws IOException
     */
    public long getFilePointer() throws IOException;

    /**
     *
     * @throws IOException
     */
    public void eof() throws IOException;

    /**
     *
     * @throws IOException
     */
    public void flush() throws IOException;
}