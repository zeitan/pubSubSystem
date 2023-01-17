package com.antonbas.pubSubSystem.domain;

import java.time.Instant;
public class Message {
    public final String payload;
    public final Instant expiration;
    public Message(String payload, Instant expiration) {
        this.payload = payload;
        this.expiration = expiration;
    }

}
