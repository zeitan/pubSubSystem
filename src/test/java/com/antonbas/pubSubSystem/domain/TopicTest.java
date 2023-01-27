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
        Topic topic = createTopicInfo(5);
        List<Message> messages = topic.getMessages("testKey");
        assertEquals(2, messages.size());
    }

    @Test
    public void callGetMessagesTwiceWhenThereAreSubscribersWithoutReceiveMessages()  {
        Topic topic = createTopicInfo(5);
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
        Topic topic = createTopicInfo(5);
        List<Message> messages = topic.getMessages("testKey");
        assertEquals(2, messages.size());
        Message message3 = new Message("test23", Instant.now().plusSeconds(2));
        topic.addMessage(message3);
        messages = topic.getMessages("testKey");
        assertEquals(1, messages.size());
    }


    @Test
    public void callGetMessagesBySecondTimeWhenMessagesWereServedAllSubscribers() {
        Topic topic = createTopicInfo(5);
        topic.addSubscribers();
        List<Message> messages = topic.getMessages("testKey");
        assertEquals(2, messages.size());
        messages = topic.getMessages("testKey");
        assertEquals(0, messages.size());
        topic.addSubscribers();
        messages = topic.getMessages("testKey2");
        assertEquals(0, messages.size());
    }

    @Test
    public void callGetMessagesAfterTTLExpiration() throws InterruptedException {
        Topic topic = createTopicInfo(1);
        List<Message> messages = topic.getMessages("testKey");
        assertEquals(2, messages.size());
        Thread.sleep(1100);
        messages = topic.getMessages("testKey2");
        assertEquals(0, messages.size());
    }

    @Test
    public void callGetMessagesWithSameSubKeyConcurrently() throws InterruptedException {
        Topic topic = createTopicInfo(5);
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
        Topic topic = createTopicInfo(5);
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

    @Test
    public void callGetMessagesWithSameSubKeyConcurrentlyGettingInfoAfterExpiration() throws InterruptedException {
        Topic topic = createTopicInfo(1);
        Thread.sleep(1100);
        try (AllInterleavings allInterleavings =
                     new AllInterleavings("TestToCallGetMessagesWithSameSubKeyConcurrentlyGettingInfoAfterExpiration");) {
            while (allInterleavings.hasNext()) {
                AtomicReference<List<Message>> messages1 = null;
                String subKey = "testSubKey";
                Thread first = new Thread(() -> {
                    messages1.set(topic.getMessages(subKey));
                });
                first.start();
                List<Message> messages2 = topic.getMessages(subKey);
                first.join();
                assertEquals(0, messages1.get().size());
                assertEquals(0, messages2.size());
            }
        }
    }

    @Test
    public void callGetMessagesWithSameSubKeyConcurrentlyAndAThreadAddingMessages() throws InterruptedException {
        Topic topic = createTopicInfo(5);
        try (AllInterleavings allInterleavings =
                     new AllInterleavings("TestToCallGetMessagesWithSameSubKeyConcurrentlyAndAThreadAddingMessages");) {
            while (allInterleavings.hasNext()) {
                AtomicReference<List<Message>> messages1 = null;
                String subKey = "testSubKey";
                Thread first = new Thread(() -> {
                    messages1.set(topic.getMessages(subKey));
                });
                Thread second = new Thread(() -> {
                    topic.addMessage(new Message("test3", Instant.now().plusSeconds(5)));
                });
                first.start();
                second.start();
                List<Message> messages2 = topic.getMessages(subKey);
                first.join();
                second.join();
                assertEquals(2, messages1.get().size());
                assertEquals(1, messages2.size());
            }
        }
    }

    @Test
    public void callGetMessagesWithDifferentSubKeyConcurrentlyAndAThreadUnsubscribing() throws InterruptedException {
        Topic topic = createTopicInfo(5);
        topic.addSubscribers();
        topic.addSubscribers();
        try (AllInterleavings allInterleavings =
                     new AllInterleavings("TestToCallGetMessagesWithSameSubKeyConcurrentlyAndAThreadUnsubscribing");) {
            while (allInterleavings.hasNext()) {
                AtomicReference<List<Message>> messages1 = null;
                AtomicReference<List<Message>> messages2 = null;
                Thread first = new Thread(() -> {
                    messages1.set(topic.getMessages("testSubKey1"));
                });
                Thread second = new Thread(() -> {
                    messages2.set(topic.getMessages("testSubKey2"));
                });
                first.start();
                second.start();
                topic.decreaseSubscribers("testSubKey1");
                first.join();
                second.join();
                assertEquals(2, messages1.get().size());
                assertEquals(2, messages2.get().size());
            }
        }
    }

    private Topic createTopicInfo(int expiration) {
        Topic topic = new Topic();
        topic.addMessage(new Message("test1", Instant.now().plusSeconds(expiration)));
        topic.addMessage(new Message("test2", Instant.now().plusSeconds(expiration)));
        return topic;
    }

}
