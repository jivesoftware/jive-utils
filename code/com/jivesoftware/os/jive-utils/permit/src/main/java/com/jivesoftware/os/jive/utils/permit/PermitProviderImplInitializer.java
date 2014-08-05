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

import com.jivesoftware.os.jive.utils.row.column.value.store.api.DefaultRowColumnValueStoreMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.SetOfSortedMapsImplInitializer;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.CurrentTimestamper;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.primatives.IntegerTypeMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.primatives.StringTypeMarshaller;
import java.io.IOException;
import java.util.UUID;
import org.merlin.config.Config;
import org.merlin.config.defaults.IntDefault;
import org.merlin.config.defaults.StringDefault;

public class PermitProviderImplInitializer {

    static public interface PermitProviderConfig extends Config {

        @IntDefault(-1)
        public int getPool();

        public void setPool(int pool);

        @StringDefault("dev")
        public String getTableNameSpace();

        public void setTableNameSpace(String tableNameSpace);

        @StringDefault("permit.log")
        public String getTableName();

        public void setTableName(String tableName);

        @StringDefault("pid")
        public String getColumnFamilyName();

        public void setColumnFamilyName(String columnFamilyName);
    }

    public PermitProvider initPermitProvider(PermitProviderConfig config,
            SetOfSortedMapsImplInitializer<? extends Exception> setOfSortedMapsImplInitializer) throws IOException {

        String ownerId = UUID.randomUUID().toString();


        return new PermitProviderImpl(ownerId,
                setOfSortedMapsImplInitializer.initialize(
                        config.getTableNameSpace(),
                        config.getTableName(),
                        config.getColumnFamilyName(),
                        new DefaultRowColumnValueStoreMarshaller<>(
                                new StringTypeMarshaller(),
                                new StringTypeMarshaller(),
                                new IntegerTypeMarshaller(),
                                new PermitMarshaller()
                        ),
                        new CurrentTimestamper()
                ),
                new CurrentTimestamper()
        );
    }
}
