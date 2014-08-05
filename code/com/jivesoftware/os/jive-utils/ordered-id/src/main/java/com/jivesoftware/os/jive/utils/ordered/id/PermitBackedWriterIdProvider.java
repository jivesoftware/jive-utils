package com.jivesoftware.os.jive.utils.ordered.id;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.jivesoftware.os.jive.utils.permit.Permit;
import com.jivesoftware.os.jive.utils.permit.PermitConfig;
import com.jivesoftware.os.jive.utils.permit.PermitProvider;
import java.util.Arrays;
import java.util.List;

/**
 * Writer ID provider backed by PermitProvider.
 *
 * In order to use a writer ID and ensure that it hasn't expired (due to GC pause, etc), check that the writer ID is
 * still valid after it has been used to generate an ID. If it has become invalid, throw away the generated ID and start
 * over.
 *
 * The possibility exists that a context switch will occur immediately after a writer ID is generated, used, and checked
 * for validity. In this short window, the writer ID could expire and the underlying permit could be granted to another
 * service. For this reason, writer IDs are set to expire after 1 second, and it is IMPERATIVE that the permit provider
 * be configured with a longer expiration. Then, in the worst case, the permit will not expire until some time many
 * milliseconds after the (now-defunct) writer ID has been used, and there can be no conflict.
 *
 * At the moment this is enforced by convention, but if necessary we can add a condition to the constructor that it will
 * not accept PermitProviders with expiration periods less than a few seconds.
 */
public class PermitBackedWriterIdProvider implements WriterIdProvider {
    private final String tenantId;
    private final String permitGroup;
    private final PermitProvider permitProvider;
    private final PermitConfig permitConfig;

    private Optional<WriterIdState> state = Optional.absent();

    private static final int WRITER_ID_LIFETIME = 1000 * 60;

    public PermitBackedWriterIdProvider(String tenantId, String permitGroup, PermitProvider permitProvider, PermitConfig permitConfig) {
        this.tenantId = tenantId;
        this.permitGroup = permitGroup;
        this.permitProvider = permitProvider;
        this.permitConfig = permitConfig;
    }

    @Override
    public synchronized WriterId getWriterId() throws OutOfWriterIdsException {
        if (doesPermitNeedRenewed()) {
            keepAlive();
        }

        if (isWriterIdValid()) {
            return state.get().writerId;
        }

        long now = System.currentTimeMillis();
        List<Permit> permits = permitProvider.requestPermit(tenantId, permitGroup, permitConfig, 1);
        if (!permits.isEmpty()) {
            updateState(permits.get(0), now);
            return state.get().writerId;
        } else {
            throw new OutOfWriterIdsException("Permit provider has issued all available permits.");
        }
    }

    private boolean doesPermitNeedRenewed() {
        return state.isPresent() && !state.get().writerId.isValid();
    }

    private boolean isWriterIdValid() {
        return state.isPresent() && state.get().writerId.isValid();
    }

    private void keepAlive() {
        Preconditions.checkState(state.isPresent());

        long now = System.currentTimeMillis();
        List<Optional<Permit>> permits = permitProvider.renewPermit(Arrays.asList(state.get().permit));
        Optional<Permit> permit = permits.get(0);
        if (permit.isPresent()) {
            updateState(permit.get(), now);
        }
    }

    private void updateState(Permit permit, long now) {
        this.state = Optional.of(new WriterIdState(permit, now + WRITER_ID_LIFETIME));
    }

    private static class WriterIdState {
        public final WriterId writerId;
        public final Permit permit;

        WriterIdState(Permit permit, long expires) {
            this.writerId = new ExpiringWriterId(permit.id, expires);
            this.permit = permit;
        }
    }
}
