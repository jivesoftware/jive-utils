package com.jivesoftware.os.jive.utils.map.store.pages;

/**
 *
 * @author jonathan
 */
public interface PageFactory {

    /**
     *
     * @param _size
     * @return
     */
    Page allocate(long _size);

}
