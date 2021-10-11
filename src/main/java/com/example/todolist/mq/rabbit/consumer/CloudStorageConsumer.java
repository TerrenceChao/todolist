package com.example.todolist.mq.rabbit.consumer;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.common.collect.Lists;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * https://cloud.google.com/docs/authentication/production#command-line
 * https://cloud.google.com/storage/docs/access-control/making-data-public?hl=zh-tw
 */
@Slf4j
@Component
@EnableAsync
public class CloudStorageConsumer extends BaseConsumer<JSONObject> {

    @Autowired
    private Environment env;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("googleCloudStorage")
    private Storage storage;

    @Override
    public JSONObject transformMsg(byte[] msgBody) throws Exception {
        return objectMapper.readValue(msgBody, JSONObject.class);
    }

    @Override
    public void businessProcess(JSONObject payload) throws Exception {
        log.info("Google Cloud Storage 邏輯 \ntid: {}, \nfilename: {}, \nhash: {}", payload.getString("tid"), payload.getString("filename"), payload.getString("hash"));

        String bucket = Objects.requireNonNull(env.getProperty("google.cloud.storage.bucket"));
        String hash = payload.getString("hash");
        String contentType = payload.getString("contentType");

        BlobId blobId = BlobId.of(bucket, hash);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
        byte[] bytes = payload.getBytes("bytes");
        Blob blob = storage.create(blobInfo, bytes);
        String publicUrl = env.getProperty("google.cloud.storage.url") + hash;
        System.out.println("\npublicUrl > " + publicUrl + "\n");
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
