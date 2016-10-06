package com.jivesoftware.os.jive.utils.collections.bah;

import com.jivesoftware.os.jive.utils.collections.baph.BAPReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author jonathan.colt
 */
class SimpleBAPReader implements BAPReader {

    private final Map<Long, byte[]> byteArrays = new ConcurrentHashMap<>();
    private final AtomicLong pointer = new AtomicLong();

    public long allocate(byte[] byteArray) {
        long p = pointer.incrementAndGet();
        byteArrays.put(p, byteArray);
        return p;
    }

    @Override
    public byte[] byteArray(long pointer) {
        if (pointer < 0) {
            return null;
        }
        return byteArrays.get(pointer);
    }

}
