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

public class PrimativeTypeStringify extends TypeStringify {

    public PrimativeTypeStringify() {
    }

    /* Inherited. */
    @Override
    public String stringify(ConfigHandler handler, Object value) {
        try {
            return String.valueOf(value);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Throwable th) {
            throw new ConfigurationRuntimeException("Type stringify error", th);
        }
    }
}
