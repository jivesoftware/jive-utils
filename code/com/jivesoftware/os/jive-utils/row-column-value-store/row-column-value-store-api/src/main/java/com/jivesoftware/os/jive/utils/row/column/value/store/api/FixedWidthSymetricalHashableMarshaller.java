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

import com.jivesoftware.os.jive.utils.row.column.value.store.api.keys.SymetricalHashableKey;
import com.jivesoftware.os.jive.utils.row.column.value.store.marshall.api.TypeMarshaller;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import javax.crypto.NoSuchPaddingException;

/**
 *
 * @author jonathan.colt
 */
public class FixedWidthSymetricalHashableMarshaller<T> implements TypeMarshaller<T> {

    private final TypeMarshaller<T> typeMarshaller;
    private final int offset;
    private final int length;
    private final SymetricalHashableKey hasher;

    public FixedWidthSymetricalHashableMarshaller(
        TypeMarshaller<T> typeMarshaller,
        int offset,
        String seed) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, UnsupportedEncodingException, InvalidKeyException {
        this.typeMarshaller = typeMarshaller;
        this.offset = offset;
        this.length = seed.length();
        this.hasher = new SymetricalHashableKey(seed);
    }

    @Override
    public T fromBytes(byte[] hashAndBytes) throws Exception {
        return fromLexBytes(hashAndBytes);
    }

    @Override
    public byte[] toBytes(T t) throws Exception {
        return toLexBytes(t);
    }

    @Override
    public T fromLexBytes(byte[] hashAndBytes) throws Exception {
        if (hashAndBytes == null) {
            return typeMarshaller.fromBytes(hashAndBytes);
        }
        if (offset == 0 && hashAndBytes.length == length) {
            hashAndBytes = hasher.toBytes(hashAndBytes);
        } else {
            byte[] hashed = new byte[length];
            System.arraycopy(hashAndBytes, offset, hashed, 0, length);
            byte[] bytes = hasher.toBytes(hashed);
            System.arraycopy(bytes, 0, hashAndBytes, offset, length);
        }
        return typeMarshaller.fromBytes(hashAndBytes);
    }

    @Override
    public byte[] toLexBytes(T t) throws Exception {
        byte[] bytes = typeMarshaller.toLexBytes(t);
        if (bytes == null) {
            return bytes;
        }
        if (offset == 0 && bytes.length == length) {
            return hasher.toHash(bytes);
        } else {
            byte[] hashed = new byte[length];
            System.arraycopy(bytes, offset, hashed, 0, length);
            byte[] hash = hasher.toHash(hashed);
            System.arraycopy(hash, 0, bytes, offset, length);
            return bytes;
        }
    }

}
