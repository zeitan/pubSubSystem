package com.antonbas.pubSubSystem.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QueueTest {

    @Test
    public void callGetMessagesFirstTime() {
        Queue queue = new Queue();
        Message message1 = new Message("test1", Instant.now().plusSeconds(5));
        Message message2 = new Message("test2", Instant.now().plusSeconds(5));
        queue.addMessage(message1);
        queue.addMessage(message2);
        List<Message> messages = queue.getMessages("testKey");
        assertEquals(2, messages.size());
    }

    @Test
    public void callGetMessagesTwiceAfterExpirationNotRunningTicker() throws InterruptedException {
        checkingWithExpiration(2000);
    }

    @Test
    public void callGetMessagesTwiceAfterExpirationRunningTicker() throws InterruptedException {
        checkingWithExpiration(6000);
    }

    @Test
    public void callGetMessagesTwiceWhenThereAreSubscribersWithoutReceiveMessages() throws InterruptedException {
        Queue queue = new Queue();
        Message message1 = new Message("test1", Instant.now().plusSeconds(2));
        Message message2 = new Message("test2", Instant.now().plusSeconds(2));
        queue.addMessage(message1);
        queue.addMessage(message2);
        queue.addSubscribers();
        queue.addSubscribers();
        List<Message> messages = queue.getMessages("testKey");
        assertEquals(2, messages.size());
        Thread.sleep(1000);
        messages = queue.getMessages("testKey");
        assertEquals(2, messages.size());
    }

    @Test
    public void callGetMessagesTwiceAndReceivingNewMessages() {
        Queue queue = new Queue();
        Message message1 = new Message("test1", Instant.now().plusSeconds(2));
        Message message2 = new Message("test2", Instant.now().plusSeconds(2));
        queue.addMessage(message1);
        queue.addMessage(message2);
        List<Message> messages = queue.getMessages("testKey");
        assertEquals(2, messages.size());
        Message message3 = new Message("test23", Instant.now().plusSeconds(2));
        queue.addMessage(message3);
        messages = queue.getMessages("testKey");
        assertEquals(1, messages.size());
    }

    private void checkingWithExpiration(long millis) throws InterruptedException {
        Queue queue = new Queue();
        Message message1 = new Message("test1", Instant.now().plusSeconds(1));
        Message message2 = new Message("test2", Instant.now().plusSeconds(1));
        queue.addMessage(message1);
        queue.addMessage(message2);
        List<Message> messages = queue.getMessages("testKey");
        assertEquals(2, messages.size());
        Thread.sleep(millis);
        messages = queue.getMessages("testKey");
        assertEquals(0, messages.size());
    }
}
