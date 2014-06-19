package com.jivesoftware.os.jive.utils.id;

public class ChainingVersioner {

    public ChainedVersion nextVersion(ChainedVersion version) {
        return new ChainedVersion(version.getVersion(), Long.toString(System.currentTimeMillis()));
    }
}
