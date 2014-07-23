package com.jivesoftware.os.jive.utils.row.column.value.store.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jivesoftware.os.jive.utils.base.interfaces.CallbackStream;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.ColumnValueAndTimestamp;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.KeyedColumnValueCallbackStream;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumValueTimestampAdd;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnTimestampRemove;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.ConstantTimestamper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.testng.Assert;

abstract public class BaseRowColumnValueStore<E extends Exception> {

    private RowColumnValueStore<String, String, String, String, E> store;
    private String tenantId = "tenantId";

    public BaseRowColumnValueStore() {
    }

    public void setStore(RowColumnValueStore<String, String, String, String, E> store) {
        this.store = store;
    }

    public RowColumnValueStore<String, String, String, String, E> getStore() {
        return store;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public void testAdd() throws E {
        String got = store.get(tenantId, "rowKey1", "columnKey1", null, null);
        Assert.assertNull(got);
        store.add(tenantId, "rowKey1", "columnKey1", "foo", null, new ConstantTimestamper(10));
        got = store.get(tenantId, "rowKey1", "columnKey1", null, null);
        Assert.assertEquals(got, "foo");

        // update with earlier timestamp
        store.add(tenantId, "rowKey1", "columnKey1", "bar", null, new ConstantTimestamper(9));
        got = store.get(tenantId, "rowKey1", "columnKey1", null, null);
        Assert.assertEquals(got, "foo");

        // update with later timestamp
        store.add(tenantId, "rowKey1", "columnKey1", "bar", null, new ConstantTimestamper(11));
        got = store.get(tenantId, "rowKey1", "columnKey1", null, null);
        Assert.assertEquals(got, "bar");
    }

    public void testCheckAndAdd() throws E {
        String got = store.get(tenantId, "rowKey1", "columnKey1", null, null);
        Assert.assertNull(got);
        Assert.assertTrue(store.addIfNotExists(tenantId, "rowKey1", "columnKey1", "foo", null, new ConstantTimestamper(10)));
        got = store.get(tenantId, "rowKey1", "columnKey1", null, null);
        Assert.assertEquals(got, "foo");

        // try again now there is a pre-existing value
        Assert.assertFalse(store.addIfNotExists(tenantId, "rowKey1", "columnKey1", "foo", null, new ConstantTimestamper(10)));
    }

    public void testMultiAdd() throws E {
        String got = store.get(tenantId, "rowKey1", "columnKey1", null, null);
        Assert.assertNull(got);
        got = store.get(tenantId, "rowKey1", "columnKey2", null, null);
        Assert.assertNull(got);

        store.multiAdd(tenantId, "rowKey1",
            new String[]{ "columnKey1", "columnKey2" },
            new String[]{ "foo", "bar" }, null, new ConstantTimestamper(10));
        got = store.get(tenantId, "rowKey1", "columnKey1", null, null);
        Assert.assertEquals(got, "foo");
        got = store.get(tenantId, "rowKey1", "columnKey2", null, null);
        Assert.assertEquals(got, "bar");

        // update with earlier timestamp
        store.multiAdd(tenantId, "rowKey1",
            new String[]{ "columnKey1", "columnKey2" },
            new String[]{ "red", "green" }, null, new ConstantTimestamper(9));
        got = store.get(tenantId, "rowKey1", "columnKey1", null, null);
        Assert.assertEquals(got, "foo");
        got = store.get(tenantId, "rowKey1", "columnKey2", null, null);
        Assert.assertEquals(got, "bar");

        // update with later timestamp
        store.multiAdd(tenantId, "rowKey1",
            new String[]{ "columnKey1", "columnKey2" },
            new String[]{ "red", "green" }, null, new ConstantTimestamper(11));
        got = store.get(tenantId, "rowKey1", "columnKey1", null, null);
        Assert.assertEquals(got, "red");
        got = store.get(tenantId, "rowKey1", "columnKey2", null, null);
        Assert.assertEquals(got, "green");
    }

    public void testMultiRowsMultiAdd() throws E {
        List<RowColumValueTimestampAdd<String, String, String>> add = new ArrayList<>();
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey1", "a", new ConstantTimestamper(5)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey1", "b", new ConstantTimestamper(6)));
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey2", "c", new ConstantTimestamper(7)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey2", "d", new ConstantTimestamper(8)));
        store.multiRowsMultiAdd(tenantId, add);

        String got = store.get(tenantId, "rowKey1", "columnKey1", null, null);
        Assert.assertEquals(got, "a");
        got = store.get(tenantId, "rowKey2", "columnKey1", null, null);
        Assert.assertEquals(got, "b");
        got = store.get(tenantId, "rowKey1", "columnKey2", null, null);
        Assert.assertEquals(got, "c");
        got = store.get(tenantId, "rowKey2", "columnKey2", null, null);
        Assert.assertEquals(got, "d");
    }

    public void testMultiGet() throws E {
        List<RowColumValueTimestampAdd<String, String, String>> add = new ArrayList<>();
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey1", "a", new ConstantTimestamper(5)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey1", "b", new ConstantTimestamper(6)));
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey2", "c", new ConstantTimestamper(7)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey2", "d", new ConstantTimestamper(8)));
        store.multiRowsMultiAdd(tenantId, add);

        List<String> got = store.multiGet(tenantId, "rowKey1", new String[]{ "columnKey1", "columnKey2" }, null, null);
        Assert.assertEquals(got.get(0), "a");
        Assert.assertEquals(got.get(1), "c");

        got = store.multiGet(tenantId, "rowKey1", new String[]{ "miss2", "miss2" }, null, null);
        Assert.assertNull(got.get(0));
        Assert.assertNull(got.get(1));

    }

    public void testMultiGetEntries() throws E {
        List<RowColumValueTimestampAdd<String, String, String>> add = new ArrayList<>();
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey1", "a", new ConstantTimestamper(5)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey1", "b", new ConstantTimestamper(6)));
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey2", "c", new ConstantTimestamper(7)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey2", "d", new ConstantTimestamper(8)));
        store.multiRowsMultiAdd(tenantId, add);

        ColumnValueAndTimestamp<String, String, Long>[] got =
            store.multiGetEntries(tenantId, "rowKey1", new String[]{ "columnKey1", "columnKey2" }, null, null);
        Assert.assertEquals(got[0].getValue(), "a");
        Assert.assertEquals(got[1].getValue(), "c");

        got = store.multiGetEntries(tenantId, "rowKey1", new String[]{ "miss2", "miss2" }, null, null);
        Assert.assertNull(got[0]);
        Assert.assertNull(got[1]);

    }

    public void testRemove() throws E {
        String got = store.get(tenantId, "rowKey1", "columnKey1", null, null);
        Assert.assertNull(got);
        store.add(tenantId, "rowKey1", "columnKey1", "foo", null, new ConstantTimestamper(10));
        store.add(tenantId, "rowKey2", "columnKey2", "bar", null, new ConstantTimestamper(10));
        got = store.get(tenantId, "rowKey1", "columnKey1", null, null);
        Assert.assertEquals(got, "foo");
        got = store.get(tenantId, "rowKey2", "columnKey2", null, null);
        Assert.assertEquals(got, "bar");

        // remove with earlier timestamp
        store.remove(tenantId, "rowKey1", "columnKey1", new ConstantTimestamper(9));
        got = store.get(tenantId, "rowKey1", "columnKey1", null, null);
        Assert.assertEquals(got, "foo");

        // remove with same timestamp
        store.remove(tenantId, "rowKey1", "columnKey1", new ConstantTimestamper(10));
        got = store.get(tenantId, "rowKey1", "columnKey1", null, null);
        Assert.assertNull(got);

        // remove with later timestamp
        store.remove(tenantId, "rowKey2", "columnKey2", new ConstantTimestamper(11));
        got = store.get(tenantId, "rowKey2", "columnKey2", null, null);
        Assert.assertNull(got);
    }

    public void testMultiRemove() throws E {
        List<RowColumValueTimestampAdd<String, String, String>> add = new ArrayList<>();
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey1", "a", new ConstantTimestamper(5)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey1", "b", new ConstantTimestamper(6)));
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey2", "c", new ConstantTimestamper(7)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey2", "d", new ConstantTimestamper(8)));
        store.multiRowsMultiAdd(tenantId, add);

        ColumnValueAndTimestamp<String, String, Long>[] got =
            store.multiGetEntries(tenantId, "rowKey1", new String[]{ "columnKey1", "columnKey2" }, null, null);
        Assert.assertEquals(got[0].getValue(), "a");
        Assert.assertEquals(got[1].getValue(), "c");

        store.multiRemove(tenantId, "rowKey1", new String[]{ "columnKey1", "columnKey2" }, new ConstantTimestamper(6));
        got = store.multiGetEntries(tenantId, "rowKey1", new String[]{ "columnKey1", "columnKey2" }, null, null);
        Assert.assertNull(got[0], "got[0] = " + got[0]);
        Assert.assertEquals(got[1].getValue(), "c");

        store.multiRemove(tenantId, "rowKey1", new String[]{ "columnKey1", "columnKey2" }, new ConstantTimestamper(12));
        got = store.multiGetEntries(tenantId, "rowKey1", new String[]{ "columnKey1", "columnKey2" }, null, null);
        Assert.assertNull(got[0]);
        Assert.assertNull(got[1]);

    }

    public void testMultiRowsMultiRemove() throws E {
        List<RowColumValueTimestampAdd<String, String, String>> add = new ArrayList<>();
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey1", "a", new ConstantTimestamper(1)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey1", "b", new ConstantTimestamper(2)));
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey1", "c", new ConstantTimestamper(3)));
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey2", "d", new ConstantTimestamper(4)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey2", "e", new ConstantTimestamper(5)));
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey2", "f", new ConstantTimestamper(6)));
        store.multiRowsMultiAdd(tenantId, add);

        List<RowColumnTimestampRemove<String, String>> remove = new ArrayList<>();
        remove.add(new RowColumnTimestampRemove<>("rowKey1", "columnKey1", new ConstantTimestamper(0)));
        remove.add(new RowColumnTimestampRemove<>("rowKey3", "columnKey2", new ConstantTimestamper(7)));

        store.multiRowsMultiRemove(tenantId, remove);

        String got = store.get(tenantId, "rowKey1", "columnKey1", null, null);
        Assert.assertEquals(got, "a");
        got = store.get(tenantId, "rowKey3", "columnKey2", null, null);
        Assert.assertNull(got);

        remove.clear();
        remove.add(new RowColumnTimestampRemove<>("rowKey1", "columnKey1", new ConstantTimestamper(10)));
        remove.add(new RowColumnTimestampRemove<>("rowKey3", "columnKey2", new ConstantTimestamper(7)));
        store.multiRowsMultiRemove(tenantId, remove);

        got = store.get(tenantId, "rowKey1", "columnKey1", null, null);
        Assert.assertNull(got);
        got = store.get(tenantId, "rowKey3", "columnKey2", null, null);
        Assert.assertNull(got);
    }

    public void testRemoveRow() throws E {
        List<RowColumValueTimestampAdd<String, String, String>> add = new ArrayList<>();
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey1", "a", new ConstantTimestamper(1)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey1", "b", new ConstantTimestamper(2)));
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey1", "c", new ConstantTimestamper(3)));
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey2", "d", new ConstantTimestamper(4)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey2", "e", new ConstantTimestamper(5)));
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey2", "f", new ConstantTimestamper(6)));
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey3", "g", new ConstantTimestamper(7)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey3", "h", new ConstantTimestamper(8)));
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey3", "i", new ConstantTimestamper(9)));
        store.multiRowsMultiAdd(tenantId, add);

        store.removeRow(tenantId, "rowKey3", new ConstantTimestamper(8));

        String got = store.get(tenantId, "rowKey3", "columnKey1", null, null);
        Assert.assertNull(got);
        got = store.get(tenantId, "rowKey3", "columnKey2", null, null);
        Assert.assertNull(got);
        got = store.get(tenantId, "rowKey3", "columnKey3", null, null);
        Assert.assertEquals(got, "i");

        store.removeRow(tenantId, "rowKey3", new ConstantTimestamper(10));
        got = store.get(tenantId, "rowKey3", "columnKey3", null, null);
        Assert.assertNull(got);

    }

    public void testPermanentRemoveRow() throws E {
        List<RowColumValueTimestampAdd<String, String, String>> add = new ArrayList<>();
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey1", "c", new ConstantTimestamper(3)));
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey2", "f", new ConstantTimestamper(6)));
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey3", "i", new ConstantTimestamper(9)));
        store.multiRowsMultiAdd(tenantId, add);

        String got = store.get(tenantId, "rowKey3", "columnKey1", null, null);
        Assert.assertNotNull(got);

        store.removeRow(tenantId, "rowKey3", new ConstantTimestamper(Long.MAX_VALUE - 1));

        got = store.get(tenantId, "rowKey3", "columnKey1", null, null);
        Assert.assertNull(got);

        store.multiRowsMultiAdd(tenantId, add);

        got = store.get(tenantId, "rowKey3", "columnKey1", null, null);
        Assert.assertNull(got);
    }

    public void testGetKeys() throws E {
        List<RowColumValueTimestampAdd<String, String, String>> add = new ArrayList<>();
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey1", "a", new ConstantTimestamper(1)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey1", "b", new ConstantTimestamper(2)));
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey1", "c", new ConstantTimestamper(3)));
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey2", "d", new ConstantTimestamper(4)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey2", "e", new ConstantTimestamper(5)));
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey2", "f", new ConstantTimestamper(6)));
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey3", "g", new ConstantTimestamper(7)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey3", "h", new ConstantTimestamper(8)));
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey3", "i", new ConstantTimestamper(9)));
        store.multiRowsMultiAdd(tenantId, add);

        final String[] expected = new String[]{
            "columnKey1",
            "columnKey2",
            "columnKey3"
        };
        store.getKeys(tenantId, "rowKey1", null, Long.MAX_VALUE, 1000, false, null, null, new CallbackStream<String>() {
            int i = 0;

            @Override
            public String callback(String v) throws Exception {
                if (v != null) {
                    Assert.assertEquals(v, expected[i]);
                    i++;
                }
                return v;
            }
        });
    }

    public void testGetValues() throws E {
        List<RowColumValueTimestampAdd<String, String, String>> add = new ArrayList<>();
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey1", "a", new ConstantTimestamper(1)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey1", "b", new ConstantTimestamper(2)));
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey1", "c", new ConstantTimestamper(3)));
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey2", "d", new ConstantTimestamper(4)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey2", "e", new ConstantTimestamper(5)));
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey2", "f", new ConstantTimestamper(6)));
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey3", "g", new ConstantTimestamper(7)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey3", "h", new ConstantTimestamper(8)));
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey3", "i", new ConstantTimestamper(9)));
        store.multiRowsMultiAdd(tenantId, add);

        final String[] expected = new String[]{
            "a",
            "d",
            "g"
        };
        store.getValues(tenantId, "rowKey1", null, Long.MAX_VALUE, 1000, false, null, null, new CallbackStream<String>() {
            int i = 0;

            @Override
            public String callback(String v) throws Exception {
                if (v != null) {
                    Assert.assertEquals(v, expected[i]);
                    i++;
                }
                return v;
            }
        });
    }

    public void testGetEntries() throws E {
        List<RowColumValueTimestampAdd<String, String, String>> add = new ArrayList<>();
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey1", "a", new ConstantTimestamper(1)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey1", "b", new ConstantTimestamper(2)));
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey1", "c", new ConstantTimestamper(3)));
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey2", "d", new ConstantTimestamper(4)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey2", "e", new ConstantTimestamper(5)));
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey2", "f", new ConstantTimestamper(6)));
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey3", "g", new ConstantTimestamper(7)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey3", "h", new ConstantTimestamper(8)));
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey3", "i", new ConstantTimestamper(9)));
        store.multiRowsMultiAdd(tenantId, add);

        final String[] expectedKeys = new String[]{
            "columnKey1",
            "columnKey2",
            "columnKey3"
        };
        final String[] expectedValues = new String[]{
            "a",
            "d",
            "g"
        };
        store.getEntrys(tenantId, "rowKey1", null, Long.MAX_VALUE, 1000, false, null, null,
            new CallbackStream<ColumnValueAndTimestamp<String, String, Long>>() {
                int i = 0;

                @Override
                public ColumnValueAndTimestamp<String, String, Long> callback(ColumnValueAndTimestamp<String, String, Long> v) throws Exception {
                    if (v != null) {
                        Assert.assertEquals(v.getColumn(), expectedKeys[i]);
                        Assert.assertEquals(v.getValue(), expectedValues[i]);
                        i++;
                    }
                    return v;
                }
            });
    }

    public void testMultiRowGetAll() throws E {
        List<RowColumValueTimestampAdd<String, String, String>> add = new ArrayList<>();
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey1", "a", new ConstantTimestamper(1)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey1", "b", new ConstantTimestamper(2)));
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey1", "c", new ConstantTimestamper(3)));
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey2", "d", new ConstantTimestamper(4)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey2", "e", new ConstantTimestamper(5)));
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey2", "f", new ConstantTimestamper(6)));
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey3", "g", new ConstantTimestamper(7)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey3", "h", new ConstantTimestamper(8)));
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey3", "i", new ConstantTimestamper(9)));
        store.multiRowsMultiAdd(tenantId, add);

        final String[] rowKey1_expectedKeys = new String[]{
            "columnKey1",
            "columnKey2",
            "columnKey3"
        };
        final String[] rowKey1_expectedValues = new String[]{
            "a",
            "d",
            "g"
        };

        final String[] rowKey2_expectedKeys = new String[]{
            "columnKey1",
            "columnKey2",
            "columnKey3"
        };
        final String[] rowKey2_expectedValues = new String[]{
            "b",
            "e",
            "h"
        };

        List<KeyedColumnValueCallbackStream<String, String, String, Long>> get = new ArrayList<>();
        get.add(new KeyedColumnValueCallbackStream<>("rowKey1", new CallbackStream<ColumnValueAndTimestamp<String, String, Long>>() {
            int i = 0;

            @Override
            public ColumnValueAndTimestamp<String, String, Long> callback(ColumnValueAndTimestamp<String, String, Long> v) throws Exception {
                if (v != null) {
                    Assert.assertEquals(v.getColumn(), rowKey1_expectedKeys[i]);
                    Assert.assertEquals(v.getValue(), rowKey1_expectedValues[i]);
                    i++;
                }
                return v;
            }
        }));

        get.add(new KeyedColumnValueCallbackStream<>("rowKey2", new CallbackStream<ColumnValueAndTimestamp<String, String, Long>>() {
            int i = 0;

            @Override
            public ColumnValueAndTimestamp<String, String, Long> callback(ColumnValueAndTimestamp<String, String, Long> v) throws Exception {
                if (v != null) {
                    Assert.assertEquals(v.getColumn(), rowKey2_expectedKeys[i]);
                    Assert.assertEquals(v.getValue(), rowKey2_expectedValues[i]);
                    i++;
                }
                return v;
            }
        }));

        store.multiRowGetAll(tenantId, get);
    }

    public void testMultiRowMultiGet() throws E {
        List<RowColumValueTimestampAdd<String, String, String>> add = new ArrayList<>();
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey1", "a", new ConstantTimestamper(1)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey1", "b", new ConstantTimestamper(2)));
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey1", "c", new ConstantTimestamper(3)));
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey2", "d", new ConstantTimestamper(4)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey2", "e", new ConstantTimestamper(5)));
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey2", "f", new ConstantTimestamper(6)));
        add.add(new RowColumValueTimestampAdd<>("rowKey1", "columnKey3", "g", new ConstantTimestamper(7)));
        add.add(new RowColumValueTimestampAdd<>("rowKey2", "columnKey3", "h", new ConstantTimestamper(8)));
        add.add(new RowColumValueTimestampAdd<>("rowKey3", "columnKey3", "i", new ConstantTimestamper(9)));
        store.multiRowsMultiAdd(tenantId, add);

        List<Map<String, String>> result = store.
            multiRowMultiGet(tenantId, ImmutableList.of("rowKey1", "rowKey2"), ImmutableList.of("columnKey1", "columnKey3"),
                -1, -1);
        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(result.get(0), ImmutableMap.of("columnKey1", "a", "columnKey3", "g"));
        Assert.assertEquals(result.get(1), ImmutableMap.of("columnKey1", "b", "columnKey3", "h"));
    }
}
