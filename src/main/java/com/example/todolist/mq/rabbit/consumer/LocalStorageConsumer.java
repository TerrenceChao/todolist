package com.example.todolist.mq.rabbit.consumer;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
@EnableAsync
public class LocalStorageConsumer extends BaseConsumer<JSONObject> {

    @Autowired
    public ObjectMapper objectMapper;

    @Override
    public JSONObject transformMsg(byte[] msgBody) throws IOException {
        return objectMapper.readValue(msgBody, JSONObject.class);
    }

    @Override
    public void businessProcess(JSONObject payload) {
        log.info("\n本地儲存邏輯 \ntid: {}, \nfilename: {}, \nhash: {}\n", payload.getString("tid"), payload.getString("filename"), payload.getString("hash"));

        try {
            String filename = payload.getString("filename");
            String fileOutput = "/Users/albert/Desktop/todo-images/" + filename;
            Path path = Paths.get(fileOutput);

            byte[] bytes = payload.getBytes("bytes");
            Files.write(path, bytes);

        } catch (IOException e) {
            log.error("無法在本地端儲存圖片", e.getMessage());
        }
    }

    @Async
    @RabbitListener(
            queues = "${mq.basic.queue}",
            containerFactory = "singleListenerContainer"
    )
    public void uploadAttach(@Payload Message message, Channel channel) throws Exception {
        consumeMessage(message, channel);
    }
}
