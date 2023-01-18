package com.antonbas.pubSubSystem.exceptions;

public class NotSubscribedException extends Exception {
    public NotSubscribedException(String message) {
        super("user not subscribed:" + message);
    }
}
