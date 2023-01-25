package com.antonbas.pubSubSystem.ws;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;


public class WsMessagesInterceptor implements HandshakeInterceptor  {
    private final static String PATTERN_AND = "&";
    private final static String PATTERN_EQUALS = "=";
    private final static String PATTERN_QUESTION_MARK = "\\?";
    private final static String UTF_8 = "UTF-8";

    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {
        String path = serverHttpRequest.getURI().getPath();
        String[] topicNameWithQueryString = path.substring(path.lastIndexOf('/') + 1).split(PATTERN_QUESTION_MARK);


        // This will be added to the websocket session
        map.put("topicName", topicNameWithQueryString[0]);
        map.put("queryString", parseQueryString(topicNameWithQueryString[1]));

        return true;
    }

    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {

    }

    private static Map<String, String> parseQueryString(String queryString) {
        Map<String, String> map = new HashMap<>();
        if ((queryString == null) || (queryString.equals(""))) {
            return map;
        }
        String[] params = queryString.split(PATTERN_AND);
        for (String param : params) {
            try {
                String[] keyValuePair = param.split(PATTERN_EQUALS, 2);
                String name = URLDecoder.decode(keyValuePair[0], UTF_8);
                if (name == "") {
                    continue;
                }
                String value = keyValuePair.length > 1 ? URLDecoder.decode(
                        keyValuePair[1], UTF_8) : "";
                map.put(name, value);
            } catch (UnsupportedEncodingException e) {
                // ignore this parameter if it can't be decoded
            }
        }
        return map;
    }
}


