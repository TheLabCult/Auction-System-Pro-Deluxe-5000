package com.auction.shared.exceptions;

public class AuctionClosedException extends Exception{
    public AuctionClosedException(String msg) {
        super(msg);
    }
}
