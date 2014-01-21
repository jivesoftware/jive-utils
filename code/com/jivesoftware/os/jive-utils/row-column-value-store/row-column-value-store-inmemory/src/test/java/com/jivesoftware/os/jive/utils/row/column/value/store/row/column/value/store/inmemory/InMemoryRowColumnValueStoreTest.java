/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jivesoftware.os.jive.utils.row.column.value.store.row.column.value.store.inmemory;

import com.jivesoftware.os.jive.utils.row.column.value.store.inmemory.RowColumnValueStoreImpl;
import com.jivesoftware.os.jive.utils.row.column.value.store.tests.BaseRowColumnValueStore;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author jonathan.colt
 */
public class InMemoryRowColumnValueStoreTest extends BaseRowColumnValueStore<RuntimeException> {

    @BeforeMethod
    public void setUpMethod() throws Exception {
        setStore(new RowColumnValueStoreImpl<String, String, String, String>());
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    @Override
    public void testAdd() {
        super.testAdd();
    }

    @Test
    @Override
    public void testCheckAndAdd() {
        super.testCheckAndAdd();
    }

    @Test
    @Override
    public void testGetEntries() {
        super.testGetEntries();
    }

    @Test
    @Override
    public void testGetKeys() {
        super.testGetKeys();
    }

    @Test
    @Override
    public void testGetValues() {
        super.testGetValues();
    }

    @Test
    @Override
    public void testMultiAdd() {
        super.testMultiAdd();
    }

    @Test
    @Override
    public void testMultiGet() {
        super.testMultiGet();
    }

    @Test
    @Override
    public void testMultiGetEntries() {
        super.testMultiGetEntries();
    }

    @Test
    @Override
    public void testMultiRemove() {
        super.testMultiRemove();
    }

    @Test
    @Override
    public void testMultiRowGetAll() {
        super.testMultiRowGetAll();
    }

    @Test
    @Override
    public void testMultiRowsMultiAdd() {
        super.testMultiRowsMultiAdd();
    }

    @Test
    @Override
    public void testMultiRowsMultiRemove() {
        super.testMultiRowsMultiRemove();
    }

    @Test
    @Override
    public void testPermanentRemoveRow() {
        super.testPermanentRemoveRow();
    }

    @Test
    @Override
    public void testRemove() {
        super.testRemove();
    }

    @Test
    @Override
    public void testRemoveRow() {
        super.testRemoveRow();
    }
}
