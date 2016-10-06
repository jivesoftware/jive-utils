package com.jivesoftware.os.jive.utils.collections.bah;

import com.beust.jcommander.internal.Sets;
import com.jivesoftware.os.jive.utils.collections.baph.ConcurrentBAPHash;
import java.util.Set;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 *
 */
public class ConcurrentBAPHashTest {

    @Test
    public void testStream() throws Exception {
        SimpleBAPReader reader = new SimpleBAPReader();
        ConcurrentBAPHash<String> concurrentBAPHash = new ConcurrentBAPHash<>(10, true, 4, reader);
        Set<String> expected = Sets.newHashSet();
        for (int i = 0; i < 1_000; i++) {
            String k = String.valueOf(i);
            long keyPointer = reader.allocate(k.getBytes());
            concurrentBAPHash.put(keyPointer, k.getBytes(), k);
            expected.add(k);
        }

        Set<String> actual = Sets.newHashSet();
        concurrentBAPHash.stream((key, value) -> {
            actual.add(value);
            return true;
        });

        assertEquals(actual, expected);
    }

    @Test
    public void testRemove() throws Exception {
        SimpleBAPReader reader = new SimpleBAPReader();
        ConcurrentBAPHash<byte[]> hash = new ConcurrentBAPHash<>(3, false, 4, reader);
        int records = 3;
        for (int i = 0; i < records; i++) {
            String k = String.valueOf(i);
            byte[] bytes = k.getBytes();
            long keyPointer = reader.allocate(bytes);
            hash.put(keyPointer, bytes, bytes);
        }

        assertEquals(hash.size(), records);

        hash.stream((key, value) -> {
            hash.remove(key);
            return true;
        });

        assertEquals(hash.size(), 0);
        for (int i = 0; i < records; i++) {
            String k = String.valueOf(i);
            byte[] bytes = k.getBytes();
            byte[] got = hash.get(bytes, 0, bytes.length);
            assertNull(got);
        }
    }
}
