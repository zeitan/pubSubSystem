package com.antonbas.pubSubSystem.exceptions;

public class NonExistentTopicException extends Exception {
    public NonExistentTopicException(String message) {
        super("Topic not existent:" + message);
    }
}
