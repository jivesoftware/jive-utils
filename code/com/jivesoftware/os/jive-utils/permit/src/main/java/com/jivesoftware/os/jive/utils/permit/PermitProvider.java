package com.jivesoftware.os.jive.utils.permit;

public interface PermitProvider {
    Permit requestPermit() throws OutOfPermitsException;
}
