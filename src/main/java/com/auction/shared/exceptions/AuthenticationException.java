package com.auction.shared.exceptions;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String msg) {
        super(msg);
    }
}
