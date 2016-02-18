package com.jivesoftware.os.jive.utils.collections.bah;

import com.beust.jcommander.internal.Sets;
import java.util.Set;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 *
 */
public class ConcurrentBAHashTest {

    @Test
    public void testStream() throws Exception {
        ConcurrentBAHash<String> concurrentBAHash = new ConcurrentBAHash<>(10, true, 4);
        Set<String> expected = Sets.newHashSet();
        for (int i = 0; i < 1_000; i++) {
            String k = String.valueOf(i);
            concurrentBAHash.put(k.getBytes(), k);
            expected.add(k);
        }

        Set<String> actual = Sets.newHashSet();
        concurrentBAHash.stream((key, value) -> {
            actual.add(value);
            return true;
        });

        assertEquals(actual, expected);
    }

    @Test
    public void testRemove() throws Exception {
        ConcurrentBAHash<byte[]> hash = new ConcurrentBAHash<>(3, false, 4);
        int records = 1_000;
        for (int i = 0; i < records; i++) {
            String k = String.valueOf(i);
            byte[] bytes = k.getBytes();
            hash.put(bytes, bytes);
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
