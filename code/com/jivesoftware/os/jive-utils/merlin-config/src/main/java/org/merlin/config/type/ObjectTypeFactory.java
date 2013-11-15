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

public class ObjectTypeFactory extends TypeFactory {

    private final Class<?> _class;

    public ObjectTypeFactory(Class<?> _class) {
        this._class = _class;
    }

    /* Inherited. */
    @Override
    public Object build(ConfigHandler handler, String string) {
        try {
            return handler.getObjectStringMapper().mapStringToObject(string, _class);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Throwable th) {
            throw new ConfigurationRuntimeException("Type construct error", th);
        }
    }
}
