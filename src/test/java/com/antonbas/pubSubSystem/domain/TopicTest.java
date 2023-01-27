package com.antonbas.pubSubSystem.domain;

import com.vmlens.api.AllInterleavings;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class TopicTest {

    @Test
    public void callGetMessagesFirstTime() {
        Topic topic = new Topic();
        Message message1 = new Message("test1", Instant.now().plusSeconds(5));
        Message message2 = new Message("test2", Instant.now().plusSeconds(5));
        topic.addMessage(message1);
        topic.addMessage(message2);
        List<Message> messages = topic.getMessages("testKey");
        assertEquals(2, messages.size());
    }

    @Test
    public void callGetMessagesTwiceWhenThereAreSubscribersWithoutReceiveMessages()  {
        Topic topic = new Topic();
        Message message1 = new Message("test1", Instant.now().plusSeconds(2));
        Message message2 = new Message("test2", Instant.now().plusSeconds(2));
        topic.addMessage(message1);
        topic.addMessage(message2);
        topic.addSubscribers();
        topic.addSubscribers();
        List<Message> messages = topic.getMessages("testKey");
        assertEquals(2, messages.size());
        messages = topic.getMessages("testKey");
        assertEquals(0, messages.size());
        messages = topic.getMessages("testKey2");
        assertEquals(2, messages.size());
    }

    @Test
    public void callGetMessagesTwiceAndReceivingNewMessages() {
        Topic topic = new Topic();
        Message message1 = new Message("test1", Instant.now().plusSeconds(2));
        Message message2 = new Message("test2", Instant.now().plusSeconds(2));
        topic.addMessage(message1);
        topic.addMessage(message2);
        List<Message> messages = topic.getMessages("testKey");
        assertEquals(2, messages.size());
        Message message3 = new Message("test23", Instant.now().plusSeconds(2));
        topic.addMessage(message3);
        messages = topic.getMessages("testKey");
        assertEquals(1, messages.size());
    }

    @Test
    public void callGetMessagesAfterTTLExpiration() throws InterruptedException {
        Topic topic = new Topic();
        Message message1 = new Message("test1", Instant.now().plusSeconds(2));
        Message message2 = new Message("test2", Instant.now().plusSeconds(2));
        topic.addMessage(message1);
        topic.addMessage(message2);
        List<Message> messages = topic.getMessages("testKey");
        assertEquals(2, messages.size());
        Thread.sleep(2000);
        messages = topic.getMessages("testKey2");
        assertEquals(0, messages.size());
    }

    @Test
    public void callGetMessagesWithSameSubKeyConcurrently() throws InterruptedException {
        Topic topic = new Topic();
        Message message1 = new Message("test1", Instant.now().plusSeconds(5));
        Message message2 = new Message("test2", Instant.now().plusSeconds(5));
        topic.addMessage(message1);
        topic.addMessage(message2);
        try (AllInterleavings allInterleavings =
                     new AllInterleavings("TestToCallGetMessagesWithSameSubKeyConcurrently");) {
            while (allInterleavings.hasNext()) {
                AtomicReference<List<Message>> messages1 = null;
                String subKey = "testSubKey";
                Thread first = new Thread(() -> {
                    messages1.set(topic.getMessages(subKey));
                });
                first.start();
                List<Message> messages2 = topic.getMessages(subKey);
                first.join();
                assertEquals(2, messages1.get().size());
                assertEquals(0, messages2.size());
            }
        }

    }

    @Test
    public void callGetMessagesWithDifferentSubKeyConcurrently() throws InterruptedException {
        Topic topic = new Topic();
        Message message1 = new Message("test1", Instant.now().plusSeconds(5));
        Message message2 = new Message("test2", Instant.now().plusSeconds(5));
        topic.addMessage(message1);
        topic.addMessage(message2);
        try (AllInterleavings allInterleavings =
                     new AllInterleavings("TestToCallGetMessagesWithDifferentSubKeyConcurrently");) {
            while (allInterleavings.hasNext()) {
                AtomicReference<List<Message>> messages1 = null;
                Thread first = new Thread(() -> {
                    messages1.set(topic.getMessages("testSubKey1"));
                });
                first.start();
                List<Message> messages2 = topic.getMessages("testSubKey2");
                first.join();
                assertEquals(2, messages1.get().size());
                assertEquals(2, messages2.size());
            }
        }

    }

}
