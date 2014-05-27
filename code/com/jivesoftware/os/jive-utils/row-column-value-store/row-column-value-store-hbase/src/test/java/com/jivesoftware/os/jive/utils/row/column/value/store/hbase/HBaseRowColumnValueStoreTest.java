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
package com.jivesoftware.os.jive.utils.row.column.value.store.hbase;

import com.jivesoftware.os.jive.utils.row.column.value.store.api.DefaultRowColumnValueStoreMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.SetOfSortedMapsImplInitializer;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.CurrentTimestamper;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.primatives.StringTypeMarshaller;
import com.jivesoftware.os.jive.utils.row.column.value.store.tests.BaseRowColumnValueStore;
import java.util.UUID;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author jonathan.colt
 */
@Test(groups = "slow")
public class HBaseRowColumnValueStoreTest extends BaseRowColumnValueStore {

    private final EmbeddedHBase embeddedHBase = new EmbeddedHBase();

    @BeforeClass
    public void startHBase() throws Exception {
        embeddedHBase.start(true);
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        String env = UUID.randomUUID().toString();
        final SetOfSortedMapsImplInitializer<Exception> hBase = new HBaseSetOfSortedMapsImplInitializer(
                embeddedHBase.getConfiguration());
        RowColumnValueStore<String, String, String, String, Exception> store =
                hBase.initialize(env, "table", "columnFamily",
                new DefaultRowColumnValueStoreMarshaller<>(new StringTypeMarshaller(),
                new StringTypeMarshaller(), new StringTypeMarshaller(),
                new StringTypeMarshaller()), new CurrentTimestamper());
        setStore(store);

    }

    @AfterClass
    public void stopHBase() throws Exception {
        embeddedHBase.stop();
    }

    @Test(groups = "slow")
    @Override
    public void testAdd() throws Exception {
        super.testAdd();
    }

    @Test(groups = "slow")
    @Override
    public void testGetEntries() throws Exception {
        super.testGetEntries();
    }

    @Test(groups = "slow")
    @Override
    public void testGetKeys() throws Exception {
        super.testGetKeys();
    }

    @Test(groups = "slow")
    @Override
    public void testGetValues() throws Exception {
        super.testGetValues();
    }

    @Test(groups = "slow")
    @Override
    public void testMultiAdd() throws Exception {
        super.testMultiAdd();
    }

    @Test(groups = "slow")
    @Override
    public void testMultiGet() throws Exception {
        super.testMultiGet();
    }

    @Test(groups = "slow")
    @Override
    public void testMultiRowMultiGet() throws Exception {
        super.testMultiRowMultiGet();
    }

    @Test(groups = "slow")
    @Override
    public void testMultiGetEntries() throws Exception {
        super.testMultiGetEntries();
    }

    @Test(groups = "slow")
    @Override
    public void testMultiRemove() throws Exception {
        super.testMultiRemove();
    }

    @Test(groups = "slow")
    @Override
    public void testMultiRowGetAll() throws Exception {
        super.testMultiRowGetAll();
    }

    @Test(groups = "slow")
    @Override
    public void testMultiRowsMultiAdd() throws Exception {
        super.testMultiRowsMultiAdd();
    }

    @Test(groups = "slow")
    @Override
    public void testMultiRowsMultiRemove() throws Exception {
        super.testMultiRowsMultiRemove();
    }

    @Test(groups = "slow")
    @Override
    public void testRemove() throws Exception {
        super.testRemove();
    }

    @Test(groups = "slow")
    @Override
    public void testRemoveRow() throws Exception {
        super.testRemoveRow();
    }

    @Test(groups = "slow")
    @Override
    public void testPermanentRemoveRow() throws Exception {
        super.testPermanentRemoveRow();
    }

    @Test(groups = "slow")
    @Override
    public void testCheckAndAdd() throws Exception {
        super.testCheckAndAdd();
    }
}
