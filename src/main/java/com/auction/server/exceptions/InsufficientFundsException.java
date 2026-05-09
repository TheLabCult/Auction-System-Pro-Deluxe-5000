package com.auction.shared.exceptions;

public class InsufficientFundsException extends Exception{
    public InsufficientFundsException(String msg) {
        super(msg);
    }
}
