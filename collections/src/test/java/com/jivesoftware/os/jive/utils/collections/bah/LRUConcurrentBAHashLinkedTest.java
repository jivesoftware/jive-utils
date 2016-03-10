package com.jivesoftware.os.jive.utils.collections.bah;

import com.beust.jcommander.internal.Sets;
import java.util.Set;
import junit.framework.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 *
 */
public class LRUConcurrentBAHashLinkedTest {

    @Test
    public void test() throws Exception {
        LRUConcurrentBAHLinkedHash<String> lruCache = new LRUConcurrentBAHLinkedHash<>(10, 100, 0.2f, true, 4);
        lruCache.start("lru", 10, (t) -> {
            Assert.fail();
            return false;
        });
        Set<String> expected = Sets.newHashSet();
        for (int i = 0; i < 1_000; i++) {
            String k = String.valueOf(i);
            lruCache.put(k.getBytes(), k);
            expected.add(k);
        }

        Set<String> actual = Sets.newHashSet();
        lruCache.stream((key, value) -> {
            actual.add(value);
            return true;
        });
        System.out.println(actual.size() + " vs " + expected.size());
        assertTrue(actual.size() < expected.size());

        lruCache.stop();
    }
}
