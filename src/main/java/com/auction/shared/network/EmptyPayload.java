package com.auction.shared.network;

public class EmptyPayload {

    // Singleton instance - no need to ever create more than one.
    public static final EmptyPayload INSTANCE = new EmptyPayload();

    private EmptyPayload() {}
}
