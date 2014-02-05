
package com.jivesoftware.os.jive.utils.chunk.store.filers;

import java.io.IOException;

/**
 *
 * @author Administrator
 */
public interface IReadable extends ICloseable {

    /**
     *
     * @return
     * @throws IOException
     */
    public int read() throws IOException;

    /**
     *
     * @param b
     * @return
     * @throws IOException
     */
    public int read(byte b[]) throws IOException;

    /**
     *
     * @param b
     * @param _offset
     * @param _len
     * @return
     * @throws IOException
     */
    public int read(byte b[], int _offset, int _len) throws IOException;
}