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

    public  List<Message> getMessages(String subKey) {
        synchronized (this.messages) {
            clearExpiredElementsFromQueue();
            if (this.messages.size() == 0)
                return Collections.EMPTY_LIST;
            int currentSize = this.messages.size();
            if (!consumerServed.containsKey(subKey)) {
                consumerServed.putIfAbsent(subKey, currentSize - 1);
                return this.messages;
            } else {
                int lastSize = consumerServed.get(subKey);
                int newSize = currentSize - 1;
                if (lastSize != newSize) {
                    consumerServed.put(subKey, newSize);
                    return IntStream.range(lastSize, newSize).mapToObj(this.messages::get)
                            .filter(x -> x.expiration.compareTo(Instant.now()) > 0).collect(Collectors.toList());
                } else
                    return Collections.EMPTY_LIST;

            }
        }
    }

    public void addSubscribers() {
        numSubscribers.incrementAndGet();
    }

    public void decreaseSubscribers(String subKey) {
        consumerServed.remove(subKey);
        numSubscribers.decrementAndGet();
    }

    public synchronized void addMessage(Message message) {
        messages.add(message);
    }

    private void clearExpiredElementsFromQueue() {
        if (this.messages.size() == 0)
            return;
        //messages were served for all the consumers subscribed;
        if(numSubscribers.get() > 0 && consumerServed.size() >= numSubscribers.get() &&
                consumerServed.values().stream().filter(x -> x + 1 ==  this.messages.size() ).count() == numSubscribers.get()) {
            this.messages.clear();
            this.consumerServed.clear();
            return;
        }
        this.messages.removeIf( x -> x.expiration.compareTo(Instant.now()) < 0 );
        this.consumerServed.replaceAll((key, currentValue)  -> (this.messages.size() -1 < currentValue ) ? this.messages.size() -1 : currentValue );
    }

}
