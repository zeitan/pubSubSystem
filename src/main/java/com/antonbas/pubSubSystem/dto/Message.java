package com.antonbas.pubSubSystem.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Message {
    private String payload;
    private int duration;

    public Message(String payload, int duration) {
        this.payload = payload;
        this.duration = duration;
    }
}
