/*
 * Copyright 2014 Jive Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jivesoftware.os.jive.utils.hwal.read;

import com.google.common.base.Optional;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import com.jivesoftware.os.jive.utils.permit.Permit;
import com.jivesoftware.os.jive.utils.permit.PermitConfig;
import com.jivesoftware.os.jive.utils.permit.PermitProvider;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author jonathan
 */
public class WALReaders {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();
    private final String readerGroupId;
    private final PermitProvider permitProvider;
    private final PermitConfig permitConfig;
    private final AtomicReference<Permit> currentOnlineId = new AtomicReference<>(null);

    public WALReaders(String readerGroupId, PermitProvider permitProvider, PermitConfig permitConfig) {
        this.readerGroupId = readerGroupId;
        this.permitProvider = permitProvider;
        this.permitConfig = permitConfig;
    }

    public Optional<Integer> getCurrentId() {
        Permit permit = currentOnlineId.get();
        if (permit == null || !permitProvider.isPermitStillValid(permit)) {
            return Optional.absent();
        }
        return Optional.of(permit.id);
    }

    public int getNumberOfOnlineWALReaders() {
        return permitProvider.getNumberOfActivePermitHolders(readerGroupId, permitConfig);
    }

    /**
     * Call this at some periodic interval
     */
    public void online() {
        Permit currentPermit = currentOnlineId.get();
        if (currentPermit == null) {
            List<Permit> permit = permitProvider.requestPermit(readerGroupId, permitConfig, 1);
            if (!permit.isEmpty()) {
                currentOnlineId.set(permit.get(0));
                LOG.info("Cursors coming on line. id:" + permit.get(0));
            }
        } else {
            List<Optional<Permit>> renewedPermit = permitProvider.renewPermit(Arrays.asList(currentPermit));
            if (renewedPermit.get(0).isPresent()) {
                currentOnlineId.set(renewedPermit.get(0).get());
            } else {
                currentOnlineId.set(null);
                LOG.warn("Was not able to renew online permit.");
            }
        }
    }

    public void offline() {
        Permit had = currentOnlineId.getAndSet(null);
        if (had != null) {
            permitProvider.releasePermit(Arrays.asList(had));
        }
    }
}
