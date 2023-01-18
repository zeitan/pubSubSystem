package com.antonbas.pubSubSystem.domain;

import com.antonbas.pubSubSystem.exceptions.NonExistentTopicException;
import com.antonbas.pubSubSystem.exceptions.NotSubscribedException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PubSub {
    //subKeys, List of topics (channels)
    private static final  Map<String, Set<String>> subscriptions =  new ConcurrentHashMap<>();;
    //topic_name(channel), List of messages
    private final static Map<String, Queue> queues = new ConcurrentHashMap<>();;

    private static final PubSub pubSubSingleton = new PubSub();

    private PubSub() {
    }

    public static PubSub getInstance() {
        return pubSubSingleton;
    }

    public synchronized void subscribe(String topicName, String subKey) throws NonExistentTopicException  {
        if (!queues.containsKey(topicName))
            throw new NonExistentTopicException(topicName);
        Set<String>  topicsSubscribed = subscriptions.getOrDefault(subKey, new HashSet<>());
        topicsSubscribed.add(topicName);
        subscriptions.put(subKey, topicsSubscribed);
        Queue queue = queues.get(topicName);
        queue.addSubscribers();
    }

    public void publish(String topicName, Message message) throws NonExistentTopicException {
        if (!queues.containsKey(topicName))
            throw new NonExistentTopicException(topicName);
        Queue queue = queues.get(topicName);
        queue.addMessage(message);
        queues.put(topicName, queue);
    }

    public void createTopic(String topicName) {
        queues.putIfAbsent(topicName, new Queue(5));
    }

    public List<Message> getMessagesPerTopic(String subKey, String topicName) throws NotSubscribedException, NonExistentTopicException {
        if (!subscriptions.containsKey(subKey) || subscriptions.get(subKey).size() == 0)
            throw new NotSubscribedException(" for the topic:" + topicName );
        if (!queues.containsKey(topicName))
            throw new NonExistentTopicException(topicName);
        return queues.get(topicName).getMessages(subKey);
    }

    public Set<String> getSubscriptionsPerUser(String subKey) {
        return subscriptions.getOrDefault(subKey, Collections.EMPTY_SET);
    }

    public synchronized void removeSubscription(String subKey, String topicName) throws NonExistentTopicException  {
        if (!queues.containsKey(topicName))
            throw new NonExistentTopicException(topicName);;
        Set<String> topicsSubscribed = subscriptions.get(subKey);
        topicsSubscribed.remove(topicName);
        subscriptions.put(subKey, topicsSubscribed);
        Queue queue = queues.get(topicName);
        queue.decreaseSubscribers();
    }

}
