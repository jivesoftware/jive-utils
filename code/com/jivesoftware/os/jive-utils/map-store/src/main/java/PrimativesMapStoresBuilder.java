import com.jivesoftware.os.jive.utils.io.ByteBufferFactory;
import com.jivesoftware.os.jive.utils.io.FilerIO;
import com.jivesoftware.os.jive.utils.io.HeapByteBufferFactory;
import com.jivesoftware.os.jive.utils.map.store.BytesBytesMapStore;
import com.jivesoftware.os.jive.utils.map.store.api.KeyValueStore;

/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */
/**
 *
 * @author jonathan.colt
 */
public class PrimativesMapStoresBuilder {

    private ByteBufferFactory bufferFactory = new HeapByteBufferFactory();
    private int initialPageCapacity = 8;

    public PrimativesMapStoresBuilder() {
    }

    public PrimativesMapStoresBuilder setByteBufferFactory(ByteBufferFactory bufferFactory) {
        this.bufferFactory = bufferFactory;
        return this;
    }

    public PrimativesMapStoresBuilder setInitialPageCapacity(int initialPageCapacity) {
        this.initialPageCapacity = initialPageCapacity;
        return this;
    }

    public KeyValueStore<Long, Long> buildLongLong() {
        return new BytesBytesMapStore<Long, Long>(8, 8, initialPageCapacity, null, bufferFactory) {

            @Override
            public byte[] keyBytes(Long key) {
                return FilerIO.longBytes(key);
            }

            @Override
            public byte[] valueBytes(Long value) {
                return FilerIO.longBytes(value);
            }

            @Override
            public Long bytesKey(byte[] bytes, int offset) {
                return FilerIO.bytesLong(bytes, offset);
            }

            @Override
            public Long bytesValue(Long key, byte[] bytes, int offset) {
                return FilerIO.bytesLong(bytes, offset);
            }
        };
    }

    public KeyValueStore<Long, Integer> buildLongInt() {
        return new BytesBytesMapStore<Long, Integer>(8, 4, initialPageCapacity, null, bufferFactory) {

            @Override
            public byte[] keyBytes(Long key) {
                return FilerIO.longBytes(key);
            }

            @Override
            public byte[] valueBytes(Integer value) {
                return FilerIO.intBytes(value);
            }

            @Override
            public Long bytesKey(byte[] bytes, int offset) {
                return FilerIO.bytesLong(bytes, offset);
            }

            @Override
            public Integer bytesValue(Long key, byte[] bytes, int offset) {
                return FilerIO.bytesInt(bytes, offset);
            }
        };
    }
}
