package com.antonbas.pubSubSystem.domain;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Topic {
    private final AtomicInteger numSubscribers;
    private final List<Message> messages;
    //subKey, Pointer of the last messageServed
    private final Map<String, Integer> consumerServed;
    private final String name;
    private final static String DEFAULT_NAME = "default";

    public Topic(String name) {
        numSubscribers = new AtomicInteger();
        numSubscribers.set(0);
        messages = Collections.synchronizedList(new LinkedList<>());
        consumerServed = new ConcurrentHashMap<>();
        this.name = name;
    }

    public Topic() {
        this(DEFAULT_NAME);
    }

    public  synchronized List<Message> getMessages(String subKey) {
        clearExpiredElementsFromTopic();
        if (this.messages.size() == 0)
            return Collections.EMPTY_LIST;
        int currentSize = this.messages.size();
        List<Message> messagesToReturn;
        if (!consumerServed.containsKey(subKey)) {
            consumerServed.putIfAbsent(subKey, currentSize - 1);
            messagesToReturn = new LinkedList<>(this.messages) ;
        } else {
            int lastSize = consumerServed.get(subKey);
            int newSize = currentSize - 1;
            if (lastSize != newSize) {
                consumerServed.put(subKey, newSize);
                messagesToReturn = IntStream.range(lastSize, newSize).mapToObj(this.messages::get)
                        .filter(x -> x.getExpiration().compareTo(Instant.now()) > 0).collect(Collectors.toList());
            } else
                messagesToReturn = Collections.EMPTY_LIST;

        }
        clearTopicIfAllSubscribersWereServed();
        return messagesToReturn;
    }

    public void addSubscribers() {
        numSubscribers.incrementAndGet();
    }

    public void decreaseSubscribers(String subKey) {
        synchronized (this.numSubscribers) {
            this.consumerServed.remove(subKey);
            this.numSubscribers.decrementAndGet();
        }
    }

    public synchronized void addMessage(Message message) {
        messages.add(message);
    }

    private void clearExpiredElementsFromTopic() {
        synchronized (this.numSubscribers) {
            if (this.messages.size() == 0)
                return;
            this.messages.removeIf(x -> x.getExpiration().compareTo(Instant.now()) < 0);
            this.consumerServed.replaceAll((key, currentValue) -> (this.messages.size() - 1 < currentValue) ? this.messages.size() - 1 : currentValue);
        }
    }

    private void clearTopicIfAllSubscribersWereServed() {
        synchronized (this.numSubscribers) {
            if (this.numSubscribers.get() > 0 && this.consumerServed.size() >= this.numSubscribers.get() &&
                    this.consumerServed.values().stream().filter(x -> x + 1 == this.messages.size()).count() == this.numSubscribers.get()) {
                this.messages.clear();
                this.consumerServed.clear();
            }
        }
    }

}
