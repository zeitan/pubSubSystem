package com.antonbas.pubSubSystem.Exceptions;

public class AuthFailedTopicException extends Exception {
    public AuthFailedTopicException(String message) {
        super("not match" + message);
    }
}