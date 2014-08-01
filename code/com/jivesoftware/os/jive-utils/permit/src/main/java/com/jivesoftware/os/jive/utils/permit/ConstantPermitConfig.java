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

/**
 *
 * @author jonathan
 */
public class ConstantPermitConfig implements PermitConfig {

    private final String pool;
    private final int minId;
    private final int countIds;
    private final long expires;

    public ConstantPermitConfig(String pool, int minId, int countIds, long expires) {
        this.pool = pool;
        this.minId = minId;
        this.countIds = countIds;
        this.expires = expires;
    }

    @Override
    public String getPool() {
        return pool;
    }

    @Override
    public int getMinId() {
        return minId;
    }

    @Override
    public int getCountIds() {
        return countIds;
    }

    @Override
    public long getExpires() {
        return expires;
    }

}
