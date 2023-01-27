package com.antonbas.pubSubSystem.service;

import com.antonbas.pubSubSystem.dto.Message;
import com.antonbas.pubSubSystem.exceptions.AuthFailedTopicException;
import com.antonbas.pubSubSystem.exceptions.NonExistentTopicException;
import com.antonbas.pubSubSystem.exceptions.NotSubscribedException;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class PubSubServiceTest {
    @Test
    public void checkingMessagePublished() throws NonExistentTopicException, NoSuchAlgorithmException, AuthFailedTopicException, NotSubscribedException {
        PubSubService pubSubService = new PubSubService();
        Message messageInput = new Message("message1",  5);
        String topic = "topic_1";
        String user = "user_1";
        pubSubService.publish(topic, messageInput);
        String subKey = pubSubService.subscribe(topic, user);
        List<String> messages = pubSubService.getMessages(topic, user, subKey);
        assertFalse(messages.isEmpty());
        assertEquals(messageInput.getPayload(), messages.get(messages.size()-1));
    }

    @Test
    public void checkingSubscribeIsOk() throws NonExistentTopicException, NoSuchAlgorithmException {
        PubSubService pubSubService = new PubSubService();
        Message messageInput = new Message("message1",  5);
        String topic = "topic_1";
        String user = "user_1";
        pubSubService.publish(topic, messageInput);
        HashCreator hashCreator = new HashCreator();
        assertEquals(hashCreator.createMD5Hash(user), pubSubService.subscribe(topic, user));
    }

    @Test
    public void subscribeInvalidTopic() {
        PubSubService pubSubService = new PubSubService();
        assertThrows(NonExistentTopicException.class, () -> {
            pubSubService.subscribe("nonExistentTopic", "user1");});
    }

    @Test
    public void tryingToGetMessagesWithBadSubKey() throws NonExistentTopicException, NoSuchAlgorithmException {
        PubSubService pubSubService = new PubSubService();
        Message messageInput = new Message("message1",  5);
        String topic = "topic_1";
        String user = "user_1";
        pubSubService.publish(topic, messageInput);
        pubSubService.subscribe(topic, user);
        assertThrows(AuthFailedTopicException.class, () -> {
            pubSubService.getMessages(topic, user, "badSubKey");});
    }

    @Test
    public void checkingUnSubscribeSuccessful() throws NonExistentTopicException, NoSuchAlgorithmException, AuthFailedTopicException, NotSubscribedException {
        PubSubService pubSubService = new PubSubService();
        Message messageInput = new Message("message1",  5);
        String topic = "topic_1";
        String user = "user_1";
        pubSubService.publish(topic, messageInput);
        String subKey = pubSubService.subscribe(topic, user);
        List<String> messages = pubSubService.getMessages(topic, user, subKey);
        pubSubService.unsubscribe(topic, user, subKey);
        assertThrows(NotSubscribedException.class, () -> {
            pubSubService.getMessages(topic, user, subKey);});
    }


}
