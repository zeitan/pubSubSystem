package com.antonbas.pubSubSystem.ws;

import com.antonbas.pubSubSystem.exceptions.AuthFailedTopicException;
import com.antonbas.pubSubSystem.exceptions.NonExistentTopicException;
import com.antonbas.pubSubSystem.exceptions.NotSubscribedException;
import com.antonbas.pubSubSystem.service.PubSubService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

@Component
public class WsMessagesHandler implements WebSocketHandler {
    @Autowired
    PubSubService pubSubService;
    private ObjectMapper oMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(WebSocketSession session)
    {
        String topicName = (String) session.getAttributes().get("topicName");
        Map<String, String> userInfo = oMapper.convertValue(session.getAttributes().get("queryString"), Map.class);
        try {
            List<String> messages = pubSubService.getMessages(topicName, userInfo.get("user_id"), userInfo.get("sub_key"));
            return session
                    .send( session.receive()
                            .map(msg -> messages)
                            .map(session::textMessage)
                    );
        }
        catch(AuthFailedTopicException | NotSubscribedException afte) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, afte.getMessage());
        }
        catch(NoSuchAlgorithmException nsae) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, nsae.getMessage());
        }
        catch(NonExistentTopicException nete) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, nete.getMessage());
        }
    }

}
