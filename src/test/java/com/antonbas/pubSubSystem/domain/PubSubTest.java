package com.antonbas.pubSubSystem.domain;

import com.antonbas.pubSubSystem.exceptions.NonExistentTopicException;
import com.antonbas.pubSubSystem.exceptions.NotSubscribedException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PubSubTest {
    @Test
    public void publishingInInvalidTopic() {
        PubSub pubSub = PubSub.getInstance();
        assertThrows(NonExistentTopicException.class, () -> {
            pubSub.publish("nonExistentTopic", null);});

    }

    @Test
    public void publishingInValidTopic() throws NonExistentTopicException, NotSubscribedException {
        String topic = "topic_1";
        String subKey = "subKey";
        PubSub pubSub = PubSub.getInstance();
        pubSub.createTopic(topic);
        pubSub.subscribe(topic, subKey);
        Message message1 = new Message("test1", Instant.now().plusSeconds(5));
        pubSub.publish(topic, message1);
        List<Message> messages = pubSub.getMessagesPerTopic(subKey, topic);
        assertFalse(messages.isEmpty());
    }

    @Test
    public void subscribingInInvalidTopic() {
        PubSub pubSub = PubSub.getInstance();
        assertThrows(NonExistentTopicException.class, () -> {
            pubSub.subscribe("nonExistentTopic", null);});

    }

    @Test void gettingMessageWhenIsNotSubscribed() {
        PubSub pubSub = PubSub.getInstance();
        assertThrows(NotSubscribedException.class, () -> {
            pubSub.getMessagesPerTopic("notSubscribedKey", null);});
    }

    @Test
    public void tryingToGetMessagesAfterUnsubscribe() throws NonExistentTopicException, NotSubscribedException {
        String topic = "topic_1";
        String subKey = "subKey";
        PubSub pubSub = PubSub.getInstance();
        pubSub.createTopic(topic);
        pubSub.subscribe(topic, subKey);
        Message message1 = new Message("test1", Instant.now().plusSeconds(5));
        pubSub.publish(topic, message1);
        List<Message> messages = pubSub.getMessagesPerTopic(subKey, topic);
        assertFalse(messages.isEmpty());
        pubSub.removeSubscription(subKey, topic);
        assertThrows(NotSubscribedException.class, () -> {
            pubSub.getMessagesPerTopic(topic, topic);});
    }
}
