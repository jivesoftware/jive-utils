package com.jivesoftware.os.jive.utils.ordered.id;

/**
 *
 *
 */
public class IdGenerationException extends RuntimeException {
    private long retryWaitHint;

    public IdGenerationException() {
    }

    public IdGenerationException(String message) {
        super(message);
    }

    public IdGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdGenerationException(Throwable cause) {
        super(cause);
    }

    public IdGenerationException(long retryWaitHint) {
        this.retryWaitHint = retryWaitHint;
    }

    public IdGenerationException(long retryWaitHint, String message) {
        super(message);
        this.retryWaitHint = retryWaitHint;
    }

    public IdGenerationException(long retryWaitHint, String message, Throwable cause) {
        super(message, cause);
        this.retryWaitHint = retryWaitHint;
    }

    public IdGenerationException(long retryWaitHint, Throwable cause) {
        super(cause);
        this.retryWaitHint = retryWaitHint;
    }

    public long getRetryWaitHint() {
        return retryWaitHint;
    }
}
