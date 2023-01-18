package com.antonbas.pubSubSystem.controller;

import com.antonbas.pubSubSystem.dto.Message;
import com.antonbas.pubSubSystem.dto.UserInfo;
import com.antonbas.pubSubSystem.util.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PubSubControllerTest extends AbstractTest {
    @Test
    public void createMessage() throws Exception {
        String uri =  "/topics/topic_1";
        Message message = new Message();
        message.payload = "test1";
        message.duration = 5;

        String inputJson = super.mapToJson(message);
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(inputJson)).andReturn();

        int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);
        String content = mvcResult.getResponse().getContentAsString();
        assertEquals(content, "message published");
    }

    @Test
    public void checkSubscribe() throws Exception {
        createMessage();

        String uri =  "/topics/topic_1/subscribe";
        UserInfo userInfo = new UserInfo();
        userInfo.userId = "user1";

        String inputJson = super.mapToJson(userInfo);
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.post(uri)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(inputJson)).andReturn();

        int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);
        String content = mvcResult.getResponse().getContentAsString();
        assertEquals(content, "24c9e15e52afc47c225b757e7bee1f9d");
    }

    @Test
    public void getMessages() throws Exception {
        checkSubscribe();
        String uri =  "/topics/topic_1";
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .contentType(MediaType.APPLICATION_JSON_VALUE).param("user_id", "user1")
                .param("sub_key", "24c9e15e52afc47c225b757e7bee1f9d")).andReturn();
        int status = mvcResult.getResponse().getStatus();
        assertEquals(200, status);
        String content = mvcResult.getResponse().getContentAsString();
        List<String> messages = super.mapFromJson(content, List.class);
        assertFalse(messages.isEmpty());
        assertEquals("test1", messages.get(0));
    }
}
