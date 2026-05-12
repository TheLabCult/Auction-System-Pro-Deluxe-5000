package com.auction.server.exceptions;

public class AuctionClosedException extends Exception{
    public AuctionClosedException(String msg) {
        super(msg);
    }
}
