package com.antonbas.pubSubSystem.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
@Getter
@Setter
@NoArgsConstructor
public class Message {
    private  String payload;
    private  Instant expiration;
    public Message(String payload, Instant expiration) {
        this.payload = payload;
        this.expiration = expiration;
    }

}
