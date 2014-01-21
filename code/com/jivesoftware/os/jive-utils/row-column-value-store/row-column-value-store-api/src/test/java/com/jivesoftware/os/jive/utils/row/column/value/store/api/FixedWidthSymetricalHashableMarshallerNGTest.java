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
package com.jivesoftware.os.jive.utils.row.column.value.store.api;

import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.api.TypeMarshaller;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import javax.crypto.NoSuchPaddingException;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author jonathan.colt
 */
public class FixedWidthSymetricalHashableMarshallerNGTest {

    public FixedWidthSymetricalHashableMarshallerNGTest() {
    }

    TypeMarshaller<long[]> longArrayMarshaller;

    @BeforeMethod
    public void setUpMethod() throws Exception {

        longArrayMarshaller = new TypeMarshaller<long[]>() {

            @Override
            public long[] fromBytes(byte[] bytes) throws Exception {
                return bytesLongs(bytes);
            }

            @Override
            public byte[] toBytes(long[] t) throws Exception {
                return longsBytes(t);
            }

            @Override
            public long[] fromLexBytes(byte[] bytes) throws Exception {
                return bytesLongs(bytes);
            }

            @Override
            public byte[] toLexBytes(long[] t) throws Exception {
                return longsBytes(t);
            }
        };
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void testNoOffset() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, UnsupportedEncodingException,
            InvalidKeyException, Exception {
        FixedWidthSymetricalHashableMarshaller<long[]> instance = new FixedWidthSymetricalHashableMarshaller<>(longArrayMarshaller, 0, "12345678");

        long[] input = new long[]{34536L, 67658L};
        byte[] lexBytes = instance.toLexBytes(input);
        long[] lexLongs = bytesLongs(lexBytes);

        Assert.assertTrue(lexLongs[0] != input[0]);
        Assert.assertTrue(lexLongs[1] == input[1]);

        long[] output = instance.fromLexBytes(lexBytes);

        Assert.assertTrue(output[0] == input[0]);
        Assert.assertTrue(output[1] == input[1]);

    }

    @Test
    public void testWithOffset() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, UnsupportedEncodingException,
            InvalidKeyException, Exception {
        FixedWidthSymetricalHashableMarshaller<long[]> instance = new FixedWidthSymetricalHashableMarshaller<>(longArrayMarshaller, 8, "12345678");

        long[] input = new long[]{34536L, 67658L};
        byte[] lexBytes = instance.toLexBytes(input);
        long[] lexLongs = bytesLongs(lexBytes);

        Assert.assertTrue(lexLongs[0] == input[0]);
        Assert.assertTrue(lexLongs[1] != input[1]);

        long[] output = instance.fromLexBytes(lexBytes);

        Assert.assertTrue(output[0] == input[0]);
        Assert.assertTrue(output[1] == input[1]);

    }

    public byte[] longsBytes(long[] longs) {
        ByteBuffer buffer = ByteBuffer.allocate(longs.length * 8);
        for (long l : longs) {
            buffer.putLong(l);
        }
        return buffer.array();
    }

    public long[] bytesLongs(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.flip(); //need flip
        int l = bytes.length / 8;
        long[] longs = new long[l];
        for (int i = 0; i < l; i++) {
            longs[i] = buffer.getLong();
        }
        return longs;
    }
}
