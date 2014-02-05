package com.jivesoftware.os.jive.utils.map.store.pages;

/**
 *
 * @author jonathan.colt
 */
public class ByteArrayPageFactory implements PageFactory {

    @Override
    public Page allocate(long _size) {

        //mode,iterations,duration,size,mb
        //add,10000000,4139,10000000,280.3802852630615
        //remove,10000000,4356,10000000,280.3802852630615
        //add/remove,10000000,3147,10000000,280.3802852630615
        //contains,10000000,3130,10000000,280.3802852630615
        return new ByteArrayPage(new byte[(int) _size]);
    }
}
