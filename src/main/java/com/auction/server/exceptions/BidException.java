package com.auction.shared.exceptions;

public class BidException extends RuntimeException {
    public BidException(String message) { super(message); }
    public BidException(String message, Throwable cause) { super(message, cause); }
}
