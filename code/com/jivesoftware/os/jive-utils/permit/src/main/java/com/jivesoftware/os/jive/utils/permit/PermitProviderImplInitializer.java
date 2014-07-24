/*
 * Copyright 2013 Jive Software, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.jivesoftware.os.jive.utils.permit;

import com.jivesoftware.os.jive.utils.row.column.value.store.api.SetOfSortedMapsImplInitializer;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.TenantLengthAndTenantFirstRowColumnValueStoreMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.CurrentTimestamper;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.api.TypeMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.primatives.LongTypeMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.primatives.StringTypeMarshaller;
import org.merlin.config.Config;
import org.merlin.config.defaults.IntDefault;
import org.merlin.config.defaults.StringDefault;

import java.io.IOException;

public class PermitProviderImplInitializer {
    static public interface PermitProviderConfig extends Config {
        @IntDefault (-1)
        public int getPool();
        public void setPool(int pool);

        @StringDefault ("")
        public String getTableNameSpace();
        public void setTableNameSpace(String tableNameSpace);

        @StringDefault ("permit.log")
        public String getTableName();
        public void setTableName(String tableName);

        @StringDefault ("p")
        public String getColumnFamilyName();
        public void setColumnFamilyName(String columnFamilyName);
    }
    private final PermitProviderConfig config;

    public PermitProviderImplInitializer(PermitProviderConfig config) {
        this.config = config;
    }

    public <T> PermitProvider initialize(
            T tenantId, int minId, int countIds, long expires,
            TypeMarshaller<T> tenantIdMarshaller,
            SetOfSortedMapsImplInitializer<? extends Exception> setOfSortedMapsImplInitializer
    ) throws IOException {
        return initPermitProvider(
                tenantId, config.getPool(), minId, countIds, expires, tenantIdMarshaller, setOfSortedMapsImplInitializer
        );
    }

    public <T> PermitProvider initialize(
            T tenantId, int pool, int minId, int countIds, long expires,
            TypeMarshaller<T> tenantIdMarshaller,
            SetOfSortedMapsImplInitializer<? extends Exception> setOfSortedMapsImplInitializer
    ) throws IOException {
        return initPermitProvider(
                tenantId, pool, minId, countIds, expires, tenantIdMarshaller, setOfSortedMapsImplInitializer
        );
    }

    private <T> PermitProvider initPermitProvider(
            T tenantId, int pool, int minId, int countIds, long expires,
            TypeMarshaller<T> tenantIdMarshaller,
            SetOfSortedMapsImplInitializer<? extends Exception> setOfSortedMapsImplInitializer
    ) throws IOException {
        return new PermitProviderImpl<>(
                tenantId, pool, minId, countIds, expires,
                setOfSortedMapsImplInitializer.initialize(
                        config.getTableNameSpace(),
                        config.getTableName(),
                        config.getColumnFamilyName(),
                        new TenantLengthAndTenantFirstRowColumnValueStoreMarshaller<>(
                                tenantIdMarshaller,
                                new PermitRowKeyMarshaller(),
                                new StringTypeMarshaller(),
                                new LongTypeMarshaller()
                        ),
                        new CurrentTimestamper()
                ),
                new CurrentTimestamper()
        );
    }
}
