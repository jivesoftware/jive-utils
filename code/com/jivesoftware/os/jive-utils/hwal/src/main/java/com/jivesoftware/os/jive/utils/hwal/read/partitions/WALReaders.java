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
package com.jivesoftware.os.jive.utils.hwal.read.partitions;

import com.google.common.base.Optional;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import com.jivesoftware.os.jive.utils.permit.Permit;
import com.jivesoftware.os.jive.utils.permit.PermitProvider;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author jonathan
 */
public class WALReaders {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();

    private final PermitProvider walReaderPermitProvider;
    private final AtomicReference<Permit> currentOnlineId = new AtomicReference<>(null);

    public WALReaders(PermitProvider walReaderPermitProvider) {
        this.walReaderPermitProvider = walReaderPermitProvider;
    }

    public Optional<Integer> getCurrentId() {
        Permit permit = currentOnlineId.get();
        if (permit == null || !walReaderPermitProvider.isPermitStillValid(permit)) {
            return Optional.absent();
        }
        return Optional.of(permit.id);
    }

    public int getNumberOfOnlineWALReaders() {
        return walReaderPermitProvider.getNumberOfActivePermitHolders();
    }

    /**
     * Call this at some periodic interval
     */
    public void online() {
        Permit currentPermit = currentOnlineId.get();
        if (currentPermit == null) {
            Optional<Permit> permit = walReaderPermitProvider.requestPermit();
            if (permit.isPresent()) {
                currentOnlineId.set(permit.get());
                LOG.info("Cursors coming on line. id:" + permit.get());
            }
        } else {
            Optional<Permit> renewedPermit = walReaderPermitProvider.renewPermit(currentPermit);
            if (renewedPermit.isPresent()) {
                currentOnlineId.set(renewedPermit.get());
            } else {
                currentOnlineId.set(null);
                LOG.warn("Was not able to renew online permit.");
            }
        }
    }

    public void offline() {
        Permit had = currentOnlineId.getAndSet(null);
        if (had != null) {
            walReaderPermitProvider.releasePermit(had);
        }
    }
}
