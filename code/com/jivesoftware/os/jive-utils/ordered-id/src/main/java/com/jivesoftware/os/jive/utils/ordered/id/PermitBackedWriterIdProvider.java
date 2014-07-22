package com.jivesoftware.os.jive.utils.ordered.id;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.jivesoftware.os.jive.utils.permit.OutOfPermitsException;
import com.jivesoftware.os.jive.utils.permit.Permit;
import com.jivesoftware.os.jive.utils.permit.PermitProvider;

/**
 * Writer ID provider backed by PermitProvider.
 *
 * In order to use a writer ID and ensure that it hasn't expired (due to GC pause, etc), check that the writer ID is
 * still valid after it has been used. When a writer ID is expired, the underlying permit may still be valid. Use
 * {@link com.jivesoftware.os.jive.utils.ordered.id.PermitBackedWriterIdProvider#keepAlive()} to renew the
 * permit, and try again.
 */
public class PermitBackedWriterIdProvider implements WriterIdProvider {
    private final PermitProvider permitProvider;

    private Optional<WriterIdState> state = Optional.absent();

    private static final int WRITER_ID_LIFETIME = 1000 * 60;

    public PermitBackedWriterIdProvider(PermitProvider permitProvider) {
        this.permitProvider = permitProvider;
    }

    @Override
    public synchronized WriterId getWriterId() throws OutOfWriterIdsException {
        if (state.isPresent()) {
            WriterId writerId = state.get().writerId;
            if (!writerId.isValid()) {
                keepAlive();
            }

            if (writerId.isValid()) {
                return writerId;
            }
        }

        long now = System.currentTimeMillis();
        try {
            Permit permit = permitProvider.requestPermit();
            updateState(permit, now);
            return state.get().writerId;
        } catch (OutOfPermitsException e) {
            throw new OutOfWriterIdsException(e);
        }
    }

    private void keepAlive() {
        Preconditions.checkState(state.isPresent());

        long now = System.currentTimeMillis();
        Optional<Permit> permit = permitProvider.renewPermit(state.get().permit);
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
