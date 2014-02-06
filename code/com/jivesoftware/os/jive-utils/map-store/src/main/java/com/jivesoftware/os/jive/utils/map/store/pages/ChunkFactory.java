package com.jivesoftware.os.jive.utils.map.store.pages;

/**
 *
 * @author jonathan
 */
public interface ChunkFactory {

    /**
     *
     * @param _size
     * @return
     */
    Chunk allocate(long _size);

}
