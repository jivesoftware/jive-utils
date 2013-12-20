/*
 * $Revision: 109733 $
 * $Date: 2010-05-05 10:34:28 -0700 (Wed, 05 May 2010) $
 *
 * Copyright (C) 1999-2011 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
package com.jivesoftware.os.jive.utils.row.column.value.store.marshall.primitives;

import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.primatives.IntegerTypeMarshaller;
import org.apache.commons.lang.math.RandomUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IntegerTypeMarshallerTest {

    private IntegerTypeMarshaller marshaller;

    @BeforeMethod
    public void setUp() throws Exception {
        marshaller = new IntegerTypeMarshaller();
    }

    @Test
    public void testMarshalling() throws Exception {
        int i = RandomUtils.nextInt();
        byte[] bytes = marshaller.toBytes(i);
        int unmarshalled = marshaller.fromBytes(bytes);

        AssertJUnit.assertEquals(i, unmarshalled);
    }

    @Test
    public void testLexMarshalling() throws Exception {
        int i = RandomUtils.nextInt();
        byte[] bytes = marshaller.toLexBytes(i);
        int unmarshalled = marshaller.fromLexBytes(bytes);

        AssertJUnit.assertEquals(i, unmarshalled);
    }
}
