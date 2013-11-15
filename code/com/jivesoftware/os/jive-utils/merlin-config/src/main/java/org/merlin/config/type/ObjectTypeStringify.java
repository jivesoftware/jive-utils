/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package org.merlin.config.type;

import org.merlin.config.ConfigHandler;
import org.merlin.config.ConfigurationRuntimeException;

public class ObjectTypeStringify extends TypeStringify {

    private final Class<?> _class;

    public ObjectTypeStringify(Class<?> _class) {
        this._class = _class;
    }

    /* Inherited. */
    @Override
    public String stringify(ConfigHandler handler, Object value) {
        try {
            //System.out.println("stringify "+value+" "+value.getClass()+" "+mapper.writeValueAsString(value));
            if (!_class.isInstance(value)) {
                throw new ConfigurationRuntimeException("Expected a instanceof " + _class + " got was " + value.getClass());
            }
            return handler.getObjectStringMapper().mapObjectToString(value);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Throwable th) {
            throw new ConfigurationRuntimeException("Type stringify error", th);
        }
    }
}
