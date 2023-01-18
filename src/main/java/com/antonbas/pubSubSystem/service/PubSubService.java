package com.antonbas.pubSubSystem.service;

import com.antonbas.pubSubSystem.exceptions.AuthFailedTopicException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.antonbas.pubSubSystem.exceptions.NonExistentTopicException;
import com.antonbas.pubSubSystem.exceptions.NotSubscribedException;
import com.antonbas.pubSubSystem.domain.PubSub;
import com.antonbas.pubSubSystem.dto.Message;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;

import java.util.List;
import java.util.stream.Collectors;
import java.time.Instant;

@Service("pubSubService")
public class PubSubService {
    PubSub pubSub;
    HashCreator hashCreator;

    Logger logger = LogManager.getLogger(PubSubService.class);
    public PubSubService() {
        pubSub = PubSub.getInstance();
        hashCreator = new HashCreator();
    }

    public void publish(String topicName, Message message) throws NonExistentTopicException {
        pubSub.createTopic(topicName);
        Instant instant = Instant.now();
        Instant expiration = instant.plusSeconds(message.duration);
        pubSub.publish(topicName, new com.antonbas.pubSubSystem.domain.Message(message.payload, expiration));
    }

    public String subscribe(String topicName, String userId) throws NoSuchAlgorithmException, NonExistentTopicException{
        if (topicName == null || userId == null)
            throw new IllegalArgumentException("the values should not be null");
        try {
            String subKey = hashCreator.createMD5Hash(userId);
            pubSub.subscribe(topicName, subKey);
            return subKey;
        }
        catch(NoSuchAlgorithmException nse) {
            logger.info("NoSuchAlgorithmException-subscribe:" + nse.getMessage());
            throw  nse;
        }
        catch(NonExistentTopicException nete) {
            logger.info("NonExistentTopicException-subscribe:" + nete.getMessage());
            throw  nete;
        }
    }
    public void unsubscribe(String topicName, String userId, String subKey)
            throws NonExistentTopicException, NoSuchAlgorithmException, AuthFailedTopicException{
        try {
            if (subKey.equals(hashCreator.createMD5Hash(userId)))
                pubSub.removeSubscription(subKey, topicName);
            else
                throw  new AuthFailedTopicException("user id + sub key");

        }
        catch(NoSuchAlgorithmException nse) {
            logger.info("NoSuchAlgorithmException-unsubscribe");
            throw  nse;
        }
        catch(NonExistentTopicException nete) {
            logger.info("NonExistentTopicException-unsubscribe:" + nete.getMessage());
            throw  nete;
        }
    }

    public List<String> getMessages(String topicName, String userId, String subKey)
            throws AuthFailedTopicException, NoSuchAlgorithmException, NonExistentTopicException, NotSubscribedException{
        try {
            if (subKey.equals(hashCreator.createMD5Hash(userId)))
                return pubSub.getMessagesPerTopic(subKey, topicName).stream().map(x -> x.payload).collect(Collectors.toList());
            else
                throw  new AuthFailedTopicException("user id + sub key");

        }
        catch(NoSuchAlgorithmException nse) {
            logger.info("NoSuchAlgorithmException-getMessages");
            throw  nse;
        }
        catch(NonExistentTopicException nete) {
            logger.info("NonExistentTopicException-getMessages:" + nete.getMessage());
            throw  nete;
        }
        catch(NotSubscribedException nse) {
            logger.info("NotSubscribedException-getMessages:" + nse.getMessage());
            throw  nse;
        }
    }
}
