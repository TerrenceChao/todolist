package com.example.todolist.mq.rabbit.consumer;

import com.alibaba.fastjson.JSONObject;
import com.example.todolist.db.rmdb.repo.AttachmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.*;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Objects;


/**
 * https://cloud.google.com/docs/authentication/production#command-line
 * https://cloud.google.com/storage/docs/access-control/making-data-public?hl=zh-tw
 * https://github.com/googleapis/java-storage
 */
@Slf4j
@Component
public class CloudStorageConsumer extends BaseConsumer<JSONObject> {

    @Autowired
    private Environment env;

    @Autowired
    @Qualifier("googleCloudStorage")
    private Storage storage;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AttachmentRepository attachRepo;

    @Override
    protected JSONObject transformMsg(byte[] msgBody) throws Exception {
        return objectMapper.readValue(msgBody, JSONObject.class);
    }

    @Override
    protected void businessProcess(JSONObject payload) throws Exception {
        try {
            log.info("Uploading Google Cloud Storage \ntid: {}, \nfilename: {}, \nhash: {}", payload.getString("tid"), payload.getString("filename"), payload.getString("hash"));

            String hashcode = payload.getString("hash");
            String bucket = Objects.requireNonNull(env.getProperty("google.cloud.storage.bucket"));
            String contentType = payload.getString("contentType");

            // uploading
            BlobId blobId = BlobId.of(bucket, hashcode);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
            byte[] bytes = payload.getBytes("bytes");
            Blob blob = storage.create(blobInfo, bytes);
            String publicUrl = env.getProperty("google.cloud.storage.url") + hashcode;
            log.info("publicUrl: {}", publicUrl);

            // save into db
            attachRepo.insert(hashcode, System.currentTimeMillis());
        } catch (Exception e) {
            log.error("Uploading Google Cloud Storage -人為手動確認消費-監聽器監聽消費消息-發生異常：", e.fillInStackTrace());
            throw e;
        }
    }

    @RabbitListener(
            queues = "${mq.attach.queue}",
            containerFactory = "singleListenerContainer"
    )
    public void uploadAttach(@Payload Message message, Channel channel) throws Exception {
        consumeMessage(message, channel);
    }
}
