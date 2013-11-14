/*
 * $Revision: 142270 $
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.http.client;

import com.jivesoftware.os.jive.utils.base.service.pojo.Tenancy;

/**
 *
 * @author jonathan
 */
public interface HttpClientProvider {

    HttpClient getClient(Tenancy tenancy);
}
