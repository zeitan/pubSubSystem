package com.antonbas.pubSubSystem.Exceptions;

public class NonExistentTopicException extends Exception {
    public NonExistentTopicException(String message) {
        super("Topic not existent:" + message);
    }
}
