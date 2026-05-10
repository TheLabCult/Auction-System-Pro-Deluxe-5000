package com.auction.server.exceptions;

public class InvalidBidException extends Exception {
    public InvalidBidException(String msg) {
        super(msg);
    }
}
