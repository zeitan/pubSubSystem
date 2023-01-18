package com.antonbas.pubSubSystem.exceptions;

public class AuthFailedTopicException extends Exception {
    public AuthFailedTopicException(String message) {
        super("not match" + message);
    }
}