package com.auction.shared.exceptions;

public class AuctionException extends RuntimeException {
    public AuctionException(String message) { super(message); }
    public AuctionException(String message, Throwable cause) { super(message, cause); }
}
