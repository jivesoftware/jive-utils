/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package org.merlin.config;

/**
 *
 */
public interface ObjectStringMapper {

    String mapObjectToString(Object object);

    <T> T mapStringToObject(String string, Class<? extends T> _class);
}
