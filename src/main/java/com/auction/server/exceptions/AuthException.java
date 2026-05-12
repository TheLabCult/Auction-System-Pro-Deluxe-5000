package com.auction.server.exceptions;

public class AuthException extends RuntimeException {
    public AuthException(String msg) {
        super(msg);
    }
}
