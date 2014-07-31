package com.jivesoftware.os.jive.utils.permit;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jivesoftware.os.jive.utils.permit.PermitProviderImpl.PermitIdGenerator;
import java.util.List;
import java.util.Set;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

public class PermitIdGeneratorTest {
    private PermitIdGenerator permitIdGenerator;

    @BeforeMethod
    public void beforeMethod() {
        permitIdGenerator = new PermitIdGenerator(0, 10);
    }

    @Test
    public void testExcludesCurrentPermits() throws Exception {
        int[] current = new int[] { 1, 3, 6, 7 };
        for (int permit : current) {
            permitIdGenerator.markCurrent(permit);
        }

        List<Integer> actual = permitIdGenerator.listAvailablePermitIds();

        Set<Integer> expected = Sets.newHashSet(0, 2, 4, 5, 8, 9);
        for (int permit : expected) {
            assertTrue(actual.contains(permit), "Expected permit " + permit + " isn't in the list of available permits: " + actual);
        }

        assertEquals(actual.size(), expected.size(), "List of available permits has " + actual.size() + " elements; expected " + expected.size());
    }

    // There's a remote chance this test could flap...
    @Test
    public void testShufflesPermits() throws Exception {
        List<Integer> actual = permitIdGenerator.listAvailablePermitIds();
        List<Integer> notExpected = Lists.newArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertNotEquals(actual, notExpected, "Got back an unshuffled list of available permits.");
    }
}
