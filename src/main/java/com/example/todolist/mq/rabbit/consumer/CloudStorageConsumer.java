package com.example.todolist.mq.rabbit.consumer;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CloudStorageConsumer extends BaseConsumer<JSONObject> {

    @Autowired
    public ObjectMapper objectMapper;

    @Override
    public JSONObject transformMsg(byte[] msgBody) throws IOException {
        return objectMapper.readValue(msgBody, JSONObject.class);
    }

    @Override
    public void businessProcess(JSONObject payload) {
        log.info("Google Cloud Storage 邏輯 \ntid: {}, \nfilename: {}, \nhash: {}", payload.getString("tid"), payload.getString("filename"), payload.getString("hash"));
        // TODO ...
    }

//    @RabbitListener(
//            queues = "${mq.basic.queue}",
//            containerFactory = "singleListenerContainer"
//    )
    public void uploadAttach(@Payload Message message, Channel channel) throws Exception {
        consumeMessage(message, channel);
    }
}
