package com.jivesoftware.os.jive.utils.ordered.id;

import com.google.common.base.Optional;
import com.jivesoftware.os.jive.utils.permit.Permit;
import com.jivesoftware.os.jive.utils.permit.PermitConfig;
import com.jivesoftware.os.jive.utils.permit.PermitProvider;
import java.util.Arrays;
import java.util.List;

/**
 * Writer ID provider backed by PermitProvider.
 *
 * In order to use a writer ID and ensure that it hasn't expired (due to GC pause, etc), check that the writer ID is still valid after it has been used to
 * generate an ID. If it has become invalid, throw away the generated ID and start over.
 *
 * The possibility exists that a context switch will occur immediately after a writer ID is generated, used, and checked for validity. In this short window, the
 * writer ID could expire and the underlying permit could be granted to another service. For this reason, writer IDs are set to expire after 1 second, and it is
 * IMPERATIVE that the permit provider be configured with a longer expiration. Then, in the worst case, the permit will not expire until some time many
 * milliseconds after the (now-defunct) writer ID has been used, and there can be no conflict.
 *
 * At the moment this is enforced by convention, but if necessary we can add a condition to the constructor that it will not accept PermitProviders with
 * expiration periods less than a few seconds.
 */
public class PermitBackedWriterIdProvider implements WriterIdProvider {

    private final String tenantId;
    private final String permitGroup;
    private final PermitProvider permitProvider;
    private final PermitConfig permitConfig;

    private Optional<WriterIdState> state = Optional.absent();

    public PermitBackedWriterIdProvider(String tenantId, String permitGroup, PermitProvider permitProvider, PermitConfig permitConfig) {
        this.tenantId = tenantId;
        this.permitGroup = permitGroup;
        this.permitProvider = permitProvider;
        this.permitConfig = permitConfig;
    }

    @Override
    public synchronized WriterId getWriterId() throws OutOfWriterIdsException {
        if (state.isPresent() && !state.get().isValid()) {
            List<Optional<Permit>> permits = permitProvider.renewPermit(Arrays.asList(state.get().permit));
            Optional<Permit> permit = permits.get(0);
            if (permit.isPresent()) {
                WriterIdState writerIdState = new WriterIdState(permitProvider, permit.get());
                state = Optional.of(writerIdState);
                return writerIdState;
            }
        }

        if (state.isPresent() && state.get().isValid()) {
            return state.get();
        }

        List<Permit> permits = permitProvider.requestPermit(tenantId, permitGroup, permitConfig, 1);
        if (permits.isEmpty()) {
            throw new OutOfWriterIdsException("Permit provider has issued all available permits.");
        } else {
            WriterIdState writerIdState = new WriterIdState(permitProvider, permits.get(0));
            state = Optional.of(writerIdState);
            return writerIdState;
        }
    }

    private static class WriterIdState implements WriterId {

        private final PermitProvider permitProvider;
        private volatile Permit permit;

        WriterIdState(PermitProvider permitProvider, Permit permit) {
            this.permitProvider = permitProvider;
            this.permit = permit;
        }

        @Override
        public int getId() {
            return permit.id;
        }

        @Override
        synchronized public boolean isValid() {
            Optional<Permit> expired = permitProvider.isExpired(permit);
            if (expired.isPresent()) {
                permit = expired.get();
                return false;
            } else {
                return true;
            }
        }
    }
}
