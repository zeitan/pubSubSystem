package com.antonbas.pubSubSystem.domain;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Queue {
    Timer queueTimer;
    private AtomicInteger numSubscribers;
    private List<Message> messages;
    private Map<String, Integer> consumerServed;
    public static final int  DEFAULT_TICKER = 5;

    class queueReminder extends TimerTask {
        public void run() {
            clearExpiredElementsFromQueue();
        }
    }

    public Queue(int seconds) {
        numSubscribers = new AtomicInteger();
        numSubscribers.set(0);
        messages = Collections.synchronizedList(new LinkedList<>());
        queueTimer = new Timer();
        queueTimer.schedule(new queueReminder(), 0, seconds * 1000L);
        consumerServed = new ConcurrentHashMap<>();
    }

    public Queue(){
        new Queue(DEFAULT_TICKER);
    }

    public List<Message> getMessages(String subKey) {
        if (this.messages.size() == 0)
            return Collections.EMPTY_LIST;
        if(this.messages.stream().noneMatch(x -> x.expiration.compareTo(Instant.now()) > 0))
            return Collections.EMPTY_LIST;
        List<Message> messagesFiltered;
        if (!consumerServed.containsKey(subKey)) {
            messagesFiltered = this.messages.stream().filter(x -> x.expiration.compareTo(Instant.now()) > 0).collect(Collectors.toList());
            consumerServed.put(subKey, messagesFiltered.size() -1);
        }
        else {
            int lastSize = consumerServed.get(subKey);
            int newSize = this.messages.size() - 1;
            if (lastSize == newSize)
                messagesFiltered = this.messages.stream().filter(x -> x.expiration.compareTo(Instant.now()) > 0).collect(Collectors.toList());
            else {
                messagesFiltered  = IntStream.range(lastSize, newSize).mapToObj(i -> this.messages.get(i))
                        .filter(x -> x.expiration.compareTo(Instant.now()) > 0).collect(Collectors.toList());
                consumerServed.put(subKey, consumerServed.put(subKey, this.messages.size() -1));
            }

        }
        return messagesFiltered;
    }

    public void addSubscribers() {
        numSubscribers.incrementAndGet();
    }

    public void decreaseSubscribers() {
        numSubscribers.decrementAndGet();
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    private void clearExpiredElementsFromQueue() {
        if (this.messages.size() == 0)
            return;
        if(numSubscribers.get() > 0 && consumerServed.size() >= numSubscribers.get() &&
                consumerServed.values().stream().filter(x -> x + 1 ==  this.messages.size() ).count() == numSubscribers.get()) {
            this.messages.clear();
            this.consumerServed.clear();
            return;
        }
        this.messages = this.messages.stream().filter(x -> x.expiration.compareTo(Instant.now()) > 0).collect(Collectors.toList());
    }

}
