/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.ordered.id;

/**
 * Provides a mechanism for obtaining globaly uniqe and orderable ids
 */
public interface OrderIdProvider {

    /**
     * Get the next id.
     *
     * @return a globally unique, orderable id
     *
     */
    long nextId();
}
