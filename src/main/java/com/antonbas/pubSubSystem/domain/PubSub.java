package com.antonbas.pubSubSystem.domain;

import com.antonbas.pubSubSystem.exceptions.NonExistentTopicException;
import com.antonbas.pubSubSystem.exceptions.NotSubscribedException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PubSub {
    //subKeys, List of topics (topic_name)
    private  final Map<String, Set<String>> subscriptions =  new ConcurrentHashMap<>();;
    //topic_name(channel), List of messages
    private final Map<String, Topic> topics = new ConcurrentHashMap<>();

    private static final PubSub pubSubSingleton = new PubSub();

    private PubSub() {
    }

    public static PubSub getInstance() {
        return pubSubSingleton;
    }

    public synchronized void subscribe(String topicName, String subKey) throws NonExistentTopicException  {
        checkTopicExistence(topicName);
        Set<String> topicsSubscribed = subscriptions.getOrDefault(subKey, new HashSet<>());
        if (topicsSubscribed.add(topicName))
            topics.get(topicName).addSubscribers();
    }

    public void publish(String topicName, Message message) throws NonExistentTopicException {
        checkTopicExistence(topicName);
        topics.get(topicName).addMessage(message);
    }

    public void createTopic(String topicName) {
        topics.putIfAbsent(topicName, new Topic(topicName));
    }

    public List<Message> getMessagesPerTopic(String subKey, String topicName) throws NotSubscribedException, NonExistentTopicException {
        checkEntitiesExistence(subKey, topicName);
        return topics.get(topicName).getMessages(subKey);
    }

    public Set<String> getSubscriptionsPerUser(String subKey) {
        return subscriptions.getOrDefault(subKey, Collections.EMPTY_SET);
    }

    public synchronized void removeSubscription(String subKey, String topicName) throws NonExistentTopicException, NotSubscribedException {
        checkEntitiesExistence(subKey, topicName);
        Set<String> topicsSubscribed = subscriptions.get(subKey);
        if (topicsSubscribed.remove(topicName))
            topics.get(topicName).decreaseSubscribers(subKey);
    }

    private void checkEntitiesExistence(String subKey, String topicName) throws NotSubscribedException, NonExistentTopicException {
        if (!subscriptions.containsKey(subKey) || subscriptions.get(subKey).size() == 0)
            throw new NotSubscribedException(" for the topic:" + topicName );
        checkTopicExistence(topicName);
    }

    private void checkTopicExistence(String topicName) throws NonExistentTopicException {
        if (!topics.containsKey(topicName))
            throw new NonExistentTopicException(topicName);
    }

}
