package com.jivesoftware.os.jive.utils.ordered.id;

public interface WriterId {
    /**
     * @return the writer ID integer
     */
    int getId();

    /**
     * @return whether or not the writer ID is valid at the time of this call
     */
    boolean isValid();
}
