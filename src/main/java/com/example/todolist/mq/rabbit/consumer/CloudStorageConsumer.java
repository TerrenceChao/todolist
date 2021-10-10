package com.example.todolist.mq.rabbit.consumer;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@EnableAsync
public class CloudStorageConsumer extends BaseConsumer<JSONObject> {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Storage storage;

    @Autowired
    private Environment env;

    @Override
    public JSONObject transformMsg(byte[] msgBody) throws Exception {
        return objectMapper.readValue(msgBody, JSONObject.class);
    }

    @Override
    public void businessProcess(JSONObject payload) throws Exception {
        log.info("Google Cloud Storage 邏輯 \ntid: {}, \nfilename: {}, \nhash: {}", payload.getString("tid"), payload.getString("filename"), payload.getString("hash"));

        String filename = payload.getString("filename");

        BlobId blobId = BlobId.of(env.getProperty("bucket"), filename);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        byte[] bytes = payload.getBytes("bytes");
        storage.create(blobInfo, bytes);
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
