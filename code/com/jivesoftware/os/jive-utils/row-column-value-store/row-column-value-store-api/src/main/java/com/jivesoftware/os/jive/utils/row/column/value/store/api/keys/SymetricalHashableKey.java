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
package com.jivesoftware.os.jive.utils.row.column.value.store.api.keys;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jivesoftware.os.jive.utils.logger.MetricLogger;
import com.jivesoftware.os.jive.utils.logger.MetricLoggerFactory;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.concurrent.ExecutionException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

/**
 * This is a useful class to fight monotonically increasing numbers. We use
 * encryption to create a distribution HBase that is likely better than
 * monotonically increasing.
 */
public class SymetricalHashableKey {

    private static final MetricLogger LOG = MetricLoggerFactory.getLogger();
    private final LoadingCache<Thread, Cipher> encrypt;
    private final LoadingCache<Thread, Cipher> decrypt;
    private final int keyLengthInBytes;

    public SymetricalHashableKey(String seed) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, UnsupportedEncodingException,
        InvalidKeyException {

        final String type = "RC4";
        byte[] keyBytes = seed.getBytes("ASCII");
        keyLengthInBytes = keyBytes.length;
        final SecretKeySpec key = new SecretKeySpec(keyBytes, type);
        encrypt = CacheBuilder.newBuilder().softValues().build(new CacheLoader<Thread, Cipher>() {
            @Override
            public Cipher load(Thread thread) throws Exception {
                try {
                    Cipher cipher = Cipher.getInstance(type);
                    cipher.init(Cipher.ENCRYPT_MODE, key);
                    return cipher;
                } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException x) {
                    LOG.error("Failed makeing cipher " + this, x);
                    return null;
                }
            }
        });

        decrypt = CacheBuilder.newBuilder().softValues().build(new CacheLoader<Thread, Cipher>() {
            @Override
            public Cipher load(Thread thread) throws Exception {
                try {
                    Cipher cipher = Cipher.getInstance(type);
                    cipher.init(Cipher.DECRYPT_MODE, key);
                    return cipher;
                } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException x) {
                    LOG.error("Failed makeing cipher " + this, x);
                    return null;
                }
            }
        });
    }

    public byte[] toHash(byte[] input) throws InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, ExecutionException {
        if (input == null) {
            return null;
        }
        if (keyLengthInBytes != input.length) {
            throw new IllegalStateException("input length=" + input.length + " must be that same as the seed length=" + keyLengthInBytes);
        }
        return encrypt.get(Thread.currentThread()).update(input);
    }

    public byte[] toBytes(byte[] hash) throws InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, ExecutionException {
        if (hash == null) {
            return null;
        }
        if (keyLengthInBytes != hash.length) {
            throw new IllegalStateException("hash length=" + hash.length + " must be that same as the seed length=" + keyLengthInBytes);
        }
        return decrypt.get(Thread.currentThread()).update(hash);
    }

}
