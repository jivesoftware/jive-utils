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

package com.jivesoftware.os.jive.utils.permit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

class PermitIdGenerator {
    private final int minId;
    private final int countIds;
    private final Set<Integer> currentPermits = new TreeSet<>();

    PermitIdGenerator(int minId, int countIds) {
        this.minId = minId;
        this.countIds = countIds;
    }

    public void markCurrent(int id) {
        currentPermits.add(id);
    }

    public List<Integer> listAvailablePermitIds() {
        List<Integer> availablePermits = new ArrayList<>();
        for (int i = minId; i < minId + countIds; i++) {
            availablePermits.add(i);
        }
        availablePermits.removeAll(currentPermits);
        Collections.shuffle(availablePermits);
        return availablePermits;
    }

}
