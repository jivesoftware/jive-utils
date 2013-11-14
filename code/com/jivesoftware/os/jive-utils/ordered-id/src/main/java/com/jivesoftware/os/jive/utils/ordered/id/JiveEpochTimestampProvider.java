package com.jivesoftware.os.jive.utils.ordered.id;

public class JiveEpochTimestampProvider implements TimestampProvider {

    public static final long JIVE_EPOCH = 1349734204785L; // Mon Oct 8, 1012 EOA epoch

    @Override
    public long getTimestamp() {
        return System.currentTimeMillis() - JIVE_EPOCH;
    }
}
