package com.antonbas.pubSubSystem.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Component
@EnableWebSocket
public class WsMessagesConfig implements WebSocketConfigurer {
    @Autowired
    WsMessagesHandler handler;

    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {

        webSocketHandlerRegistry.addHandler((WebSocketHandler) handler, "/get-messages/*").addInterceptors(new WsMessagesInterceptor());

    }
}
