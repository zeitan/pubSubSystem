package com.antonbas.pubSubSystem.controller;


import com.antonbas.pubSubSystem.exceptions.AuthFailedTopicException;
import com.antonbas.pubSubSystem.exceptions.NonExistentTopicException;
import com.antonbas.pubSubSystem.dto.Message;
import com.antonbas.pubSubSystem.dto.UserInfo;
import com.antonbas.pubSubSystem.exceptions.NotSubscribedException;
import com.antonbas.pubSubSystem.service.PubSubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.NoSuchAlgorithmException;
import java.util.List;


@RestController
public class PubSubController {
    @Autowired
    PubSubService pubSubService;

    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(value = "/topics/{topic_name}")
    public ResponseEntity<String> publish(@PathVariable("topic_name") String topicName, @RequestBody Message message) {
        try {
            pubSubService.publish(topicName, message);
            return new ResponseEntity<>("message published", HttpStatus.CREATED);
        }
        catch (NonExistentTopicException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

    }

    @GetMapping(value = "/topics/{topic_name}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public List<String> getMessages(@PathVariable("topic_name") String topicName, @RequestParam("user_id") String userId, @RequestParam("sub_key") String subKey) {
        try {
            return pubSubService.getMessages(topicName, userId, subKey);
        }
        catch(AuthFailedTopicException | NotSubscribedException afte) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, afte.getMessage());
        }
        catch(NoSuchAlgorithmException nsae) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, nsae.getMessage());
        }
        catch(NonExistentTopicException nete) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, nete.getMessage());
        }
    }

    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(value = "/topics/{topic_name}/subscribe")
    public ResponseEntity<String> subscribe(@PathVariable("topic_name") String topicName, @RequestBody UserInfo userInfo) {
        try {
            return new ResponseEntity<>(pubSubService.subscribe(topicName, userInfo.getUserId()), HttpStatus.OK);
        }
        catch(NoSuchAlgorithmException nsae) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, nsae.getMessage());
        }
        catch (NonExistentTopicException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @ResponseStatus(value = HttpStatus.OK)
    @DeleteMapping(value = "/topics/{topic_name}/unsubscribe")
    public ResponseEntity<String> unsubscribe(@PathVariable("topic_name") String topicName, @RequestBody UserInfo userInfo) {
        try {
            pubSubService.unsubscribe(topicName, userInfo.getUserId(), userInfo.getSubKey());
            return new ResponseEntity<>("unsubscribed", HttpStatus.OK);
        }
        catch(AuthFailedTopicException afte) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, afte.getMessage());
        }
        catch(NoSuchAlgorithmException nsae) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, nsae.getMessage());
        }
        catch (NonExistentTopicException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

}
