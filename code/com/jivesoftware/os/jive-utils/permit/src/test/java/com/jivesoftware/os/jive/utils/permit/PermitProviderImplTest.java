package com.jivesoftware.os.jive.utils.permit;

import com.google.common.base.Optional;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.AtomicIncrementingTimestamper;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.Timestamper;
import com.jivesoftware.os.jive.utils.row.column.value.store.inmemory.RowColumnValueStoreImpl;
import java.util.Arrays;
import java.util.List;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.jivesoftware.os.jive.utils.permit.PermitProviderImpl.NULL_KEY;
import static org.mockito.Mockito.doAnswer;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;

public class PermitProviderImplTest {

    private PermitProviderImpl permitProviderImpl;
    private PermitConfig permitConfig;

    private RowColumnValueStore<String, PermitRowKey, String, Permit, RuntimeException> store;
    private Timestamper timestamper = new AtomicIncrementingTimestamper(10000);
    private long now, expired;

    private static final String TENANT = "permit-test";
    private static final String POOL = "pool";
    private static final long EXPIRES = 1000;

    @BeforeMethod
    public void beforeMethod() throws Exception {
        store = Mockito.spy(new RowColumnValueStoreImpl<String, PermitRowKey, String, Permit>());

        permitConfig = new ConstantPermitConfig(POOL, 0, 512, EXPIRES);
        permitProviderImpl = new PermitProviderImpl("bob", store, timestamper);

        now = timestamper.get();
        expired = now - EXPIRES;
    }

    @Test
    public void testReissuesExpiredPermits() throws Exception {
        addExpiredPermit(10);

        Permit actual = permitProviderImpl.requestPermit(TENANT, permitConfig, 1).get(0);

        Permit expected = new Permit(now + 1, EXPIRES, 10, "bob", TENANT, POOL);
        assertEquals(actual, expected, "Issued permit is not the one that was expired.");
    }

    @Test
    public void testDoesNotReissueCurrentPermits() throws Exception {
        addCurrentPermit(10);

        Permit actual = permitProviderImpl.requestPermit(TENANT, permitConfig, 1).get(0);

        Permit notExpected = new Permit(now + 1, EXPIRES, 10, "bob", TENANT, POOL);
        assertNotEquals(actual, notExpected, "Issued permit was still current.");
    }

    @Test
    public void testDoesNotReissueExpiredPermitsConcurrentlyIssued() throws Exception {
        addExpiredPermit(0);
        yoinkPermitRightBeforeIssue(0, new Permit(now + 1, EXPIRES, 0, "bob", TENANT, POOL), new Permit(expired, EXPIRES, 0, "bob", TENANT, POOL));

        Permit actual = permitProviderImpl.requestPermit(TENANT, permitConfig, 1).get(0);

        Permit notExpected = new Permit(now + 1, EXPIRES, 0, "bob", TENANT, POOL);
        assertNotEquals(actual, notExpected, "Issued expired permit that just got issued to another caller.");
    }

    @Test
    public void testIssuesAvailablePermitsIfNoneExpired() throws Exception {
        for (int i = 0; i < 511; i++) {
            addCurrentPermit(i);
        }

        Permit actual = permitProviderImpl.requestPermit(TENANT, permitConfig, 1).get(0);

        Permit expected = new Permit(now + 1, EXPIRES, 511, "bob", TENANT, POOL);
        assertEquals(actual, expected, "Issued a permit that was still current, or one that is outside of bounds.");
    }

    @Test
    public void testDoesNotIssueAvailablePermitConcurrentlyIssued() throws Exception {
        for (int i = 0; i < 511; i++) {
            addCurrentPermit(i);
        }

        yoinkPermitRightBeforeIssue(511, new Permit(now + 1, EXPIRES, 511, "bob", TENANT, POOL), null);

        List<Permit> got = permitProviderImpl.requestPermit(TENANT, permitConfig, 1);
        System.out.println("got:" + got);
        Assert.assertFalse(!got.isEmpty());
        // Won't issue the permit since it gets issued right before we grab it, so we get an OutOfPermitsException
    }

    @Test
    public void testRenewsCurrentPermits() throws Exception {
        addCurrentPermit(10);

        List<Optional<Permit>> actual = permitProviderImpl.renewPermit(Arrays.asList(new Permit(now, EXPIRES, 10, "bob", TENANT, POOL)));

        Optional<Permit> expected = Optional.of(new Permit(now + 1, EXPIRES, 10, "bob", TENANT, POOL));
        assertEquals(actual.get(0), expected, "Wasn't able to renew a current permit.");
    }

    @Test
    public void testDoesNotRenewPermitsWithDifferentValue() throws Exception {
        addCurrentPermit(10);

        List<Optional<Permit>> actual = permitProviderImpl.renewPermit(Arrays.asList(new Permit(expired, EXPIRES, 10, "bob", TENANT, POOL)));

        assertFalse(actual.get(0).isPresent(), "Succeeded in renewing a permit that didn't match the actual timestamp.");
    }

    @Test
    public void testDoesNotRenewExpiredPermits() throws Exception {
        addExpiredPermit(10);

        List<Optional<Permit>> actual = permitProviderImpl.renewPermit(Arrays.asList(new Permit(expired, EXPIRES, 10, "bob", TENANT, POOL)));
        System.out.println(actual);
        assertFalse(actual.get(0).isPresent(), "Succeeded in renewing a permit that was expired.");
    }

    @Test
    public void testDoesNotRenewCurrentPermitsConcurrentlyIssued() throws Exception {
        addCurrentPermit(10);
        yoinkPermitRightBeforeIssue(10, new Permit(now + 1, EXPIRES, 10, "bob", TENANT, POOL), new Permit(now, EXPIRES, 10, "bob", TENANT, POOL));

        List<Optional<Permit>> actual = permitProviderImpl.renewPermit(Arrays.asList(new Permit(now + 1, EXPIRES, 10, "bob", TENANT, POOL)));

        assertFalse(actual.get(0).isPresent(), "Succeeded in renewing a permit that just got issued to another caller.");
    }

    @Test
    public void testAllPermitsTakenThrowsOutOfPermits() throws Exception {
        for (int i = 0; i < 512; i++) {
            addCurrentPermit(i);
        }

        List<Permit> got = permitProviderImpl.requestPermit(TENANT, permitConfig, 1);
        Assert.assertTrue(got.isEmpty());
    }

    private void addCurrentPermit(int id) {
        store.add(TENANT, new PermitRowKey(POOL, id), NULL_KEY, new Permit(now, EXPIRES, id, "bob", TENANT, POOL), null, null);
    }

    private void addExpiredPermit(int id) {
        store.add(TENANT, new PermitRowKey(POOL, id), NULL_KEY, new Permit(expired, EXPIRES, id, "bob", TENANT, POOL), null, null);
    }

    private void yoinkPermitRightBeforeIssue(final int id, Permit issued, Permit expectedIssued) {
        final PermitRowKey permitRowKey = new PermitRowKey(POOL, id);
        doAnswer(new Answer<Boolean>() {
            public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
                store.add(TENANT, permitRowKey, NULL_KEY, new Permit(now + 1, EXPIRES, id, "bob", TENANT, POOL), null, null);
                return (Boolean) invocationOnMock.callRealMethod();
            }
        }).when(store).replaceIfEqualToExpected(TENANT, permitRowKey, NULL_KEY, issued, expectedIssued, null, null);
    }
}
