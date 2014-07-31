package com.jivesoftware.os.jive.utils.permit;

import com.google.common.base.Optional;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.RowColumnValueStore;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.AtomicIncrementingTimestamper;
import com.jivesoftware.os.jive.utils.row.column.value.store.api.timestamper.Timestamper;
import com.jivesoftware.os.jive.utils.row.column.value.store.inmemory.RowColumnValueStoreImpl;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.jivesoftware.os.jive.utils.permit.PermitProviderImpl.NULL_KEY;
import static org.mockito.Mockito.doAnswer;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;

public class PermitProviderImplTest {
    private PermitProviderImpl<String> permitProviderImpl;

    private RowColumnValueStore<String, PermitRowKey, String, Long, RuntimeException> store;
    private Timestamper timestamper = new AtomicIncrementingTimestamper(10000);
    private long now, expired;

    private static final String TENANT = "permit-test";
    private static final int POOL = 0;
    private static final long EXPIRES = 1000;

    @BeforeMethod
    public void beforeMethod() throws Exception {
        store = Mockito.spy(new RowColumnValueStoreImpl<String, PermitRowKey, String, Long>());
        permitProviderImpl = new PermitProviderImpl<>(TENANT, POOL, 0, 512, EXPIRES, store, timestamper);

        now = timestamper.get();
        expired = now - EXPIRES;
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRequiresCountIdsGreaterThan0() throws Exception {
        new PermitProviderImpl<>(TENANT, POOL, 0, 0, EXPIRES, store, timestamper);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRequiresPermitsExpireInTheFuture() throws Exception {
        new PermitProviderImpl<>(TENANT, POOL, 0, 0, 0, store, timestamper);
    }

    @Test
    public void testReissuesExpiredPermits() throws Exception {
        addExpiredPermit(10);

        Permit actual = permitProviderImpl.requestPermit();

        Permit expected = new Permit(POOL, 10, now + 1);
        assertEquals(actual, expected, "Issued permit is not the one that was expired.");
    }

    @Test
    public void testDoesNotReissueCurrentPermits() throws Exception {
        addCurrentPermit(10);

        Permit actual = permitProviderImpl.requestPermit();

        Permit notExpected = new Permit(POOL, 10, now + 1);
        assertNotEquals(actual, notExpected, "Issued permit was still current.");
    }

    @Test
    public void testDoesNotReissueExpiredPermitsConcurrentlyIssued() throws Exception {
        addExpiredPermit(0);
        yoinkPermitRightBeforeIssue(0, now + 1, expired);

        Permit actual = permitProviderImpl.requestPermit();

        Permit notExpected = new Permit(POOL, 0, now + 1);
        assertNotEquals(actual, notExpected, "Issued expired permit that just got issued to another caller.");
    }

    @Test
    public void testIssuesAvailablePermitsIfNoneExpired() throws Exception {
        for (int i = 0; i < 511; i++) {
            addCurrentPermit(i);
        }

        Permit actual = permitProviderImpl.requestPermit();

        Permit expected = new Permit(POOL, 511, now + 1);
        assertEquals(actual, expected, "Issued a permit that was still current, or one that is outside of bounds.");
    }

    @Test(expectedExceptions = OutOfPermitsException.class)
    public void testDoesNotIssueAvailablePermitConcurrentlyIssued() throws Exception {
        for (int i = 0; i < 511; i++) {
            addCurrentPermit(i);
        }
        yoinkPermitRightBeforeIssue(511, now + 1, null);

        permitProviderImpl.requestPermit();

        // Won't issue the permit since it gets issued right before we grab it, so we get an OutOfPermitsException
    }

    @Test
    public void testRenewsCurrentPermits() throws Exception {
        addCurrentPermit(10);

        Optional<Permit> actual = permitProviderImpl.renewPermit(new Permit(POOL, 10, now));

        Optional<Permit> expected = Optional.of(new Permit(POOL, 10, now + 1));
        assertEquals(actual, expected, "Wasn't able to renew a current permit.");
    }

    @Test
    public void testDoesNotRenewPermitsWithDifferentValue() throws Exception {
        addCurrentPermit(10);

        Optional<Permit> actual = permitProviderImpl.renewPermit(new Permit(POOL, 10, expired));

        assertFalse(actual.isPresent(), "Succeeded in renewing a permit that didn't match the actual timestamp.");
    }

    @Test
    public void testDoesNotRenewExpiredPermits() throws Exception {
        addExpiredPermit(10);

        Optional<Permit> actual = permitProviderImpl.renewPermit(new Permit(POOL, 10, expired));

        assertFalse(actual.isPresent(), "Succeeded in renewing a permit that was expired.");
    }

    @Test
    public void testDoesNotRenewCurrentPermitsConcurrentlyIssued() throws Exception {
        addCurrentPermit(10);
        yoinkPermitRightBeforeIssue(10, now + 1, now);

        Optional<Permit> actual = permitProviderImpl.renewPermit(new Permit(POOL, 10, now));

        assertFalse(actual.isPresent(), "Succeeded in renewing a permit that just got issued to another caller.");
    }

    @Test(expectedExceptions = OutOfPermitsException.class)
    public void testAllPermitsTakenThrowsOutOfPermits() throws Exception {
        for (int i = 0; i < 512; i++) {
            addCurrentPermit(i);
        }

        permitProviderImpl.requestPermit();
    }

    private void addCurrentPermit(int id) {
        store.add(TENANT, new PermitRowKey(POOL, id), NULL_KEY, now, null, null);
    }

    private void addExpiredPermit(int id) {
        store.add(TENANT, new PermitRowKey(POOL, id), NULL_KEY, expired, null, null);
    }

    private void yoinkPermitRightBeforeIssue(int id, Long issued, Long expectedIssued) {
        final PermitRowKey permitRowKey = new PermitRowKey(POOL, id);
        doAnswer(new Answer<Boolean>() {
            public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
                store.add(TENANT, permitRowKey, NULL_KEY, now + 1, null, null);
                return (Boolean) invocationOnMock.callRealMethod();
            }
        }).when(store).replaceIfEqualToExpected(TENANT, permitRowKey, NULL_KEY, issued, expectedIssued, null, null);
    }
}
