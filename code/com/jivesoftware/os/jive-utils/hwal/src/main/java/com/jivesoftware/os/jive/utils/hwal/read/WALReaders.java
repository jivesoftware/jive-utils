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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author jonathan
 */
public class WALReaders {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();
    private final String tenantId;
    private final String readerGroupId;
    private final PermitProvider permitProvider;
    private final PermitConfig permitConfig;
    private final AtomicReference<Permit> currentOnlineId = new AtomicReference<>(null);

    public WALReaders(String tenantId, String readerGroupId, PermitProvider permitProvider, PermitConfig permitConfig) {
        this.tenantId = tenantId;
        this.readerGroupId = readerGroupId;
        this.permitProvider = permitProvider;
        this.permitConfig = permitConfig;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getReaderGroupId() {
        return readerGroupId;
    }

    public Optional<Integer> getCurrentId() {
        Permit permit = currentOnlineId.get();
        Optional<Permit> optionalPermit = permitProvider.isExpired(permit);
        if (optionalPermit.isPresent()) {
            return Optional.of(optionalPermit.get().id);
        } else {
           return Optional.absent();
        }
    }

    public int getNumberOfOnlineWALReaders() {
        int numberOfOnlineReaders = getNumberOfActivePermitHolders();
        if (numberOfOnlineReaders == 0) {
            LOG.warn("Currently no readers are online for tenantId:" + tenantId + " readerGroupId:" + readerGroupId + " permitConfig:" + permitConfig);
        }
        return numberOfOnlineReaders;
    }

    private int getNumberOfActivePermitHolders() {
        List<Permit> allIssuedPermits = permitProvider.getAllIssuedPermits(tenantId, readerGroupId, permitConfig);
        Set<String> distinctOwners = new HashSet<>();
        for (Permit permit : allIssuedPermits) {
            Optional<Permit> optionalPermit = permitProvider.isExpired(permit);
            if (optionalPermit.isPresent()) {
                distinctOwners.add(optionalPermit.get().owner);
            }
        }
        return distinctOwners.size();
    }

    /**
     * Call this at some periodic interval
     */
    public void online() {
        Permit currentPermit = currentOnlineId.get();
        if (currentPermit == null) {
            List<Permit> permit = permitProvider.requestPermit(tenantId, readerGroupId, permitConfig, 1);
            if (!permit.isEmpty()) {
                currentOnlineId.set(permit.get(0));
                LOG.info("Reader ONLINE for tenant:" + tenantId + " group:" + readerGroupId + ". reader id:" + permit.get(0));
            } else {
                LOG.error("Reader OFFLINE no available permits for tenant:" + tenantId + " group:" + readerGroupId);
            }
        } else {
            List<Optional<Permit>> renewedPermit = permitProvider.renewPermit(Arrays.asList(currentPermit));
            if (renewedPermit.get(0).isPresent()) {
                currentOnlineId.set(renewedPermit.get(0).get());
                LOG.debug("Reader RENEWED  for tenant:" + tenantId + " group:" + readerGroupId + ". reader id:" + renewedPermit.get(0));
            } else {
                currentOnlineId.set(null);
                LOG.warn("FAILED to RENEW reader tenant:" + tenantId + " group:" + readerGroupId + ". reader id:" + currentPermit);
            }
        }
    }

    public void offline() {
        Permit had = currentOnlineId.getAndSet(null);
        if (had != null) {
            permitProvider.releasePermit(Arrays.asList(had));
            LOG.info("Reader RELEASED for tenant:" + tenantId + " group:" + readerGroupId + ". reader id:" + had);
        } else {
            LOG.info("Reader already offline for tenant:" + tenantId + " group:" + readerGroupId);
        }
    }
}
