package com.example.todolist.mq.rabbit.consumer;

import com.alibaba.fastjson.JSONObject;
import com.example.todolist.service.AttachService;
import com.example.todolist.service.TodoService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Intro to Google Cloud Storage with Java
 * https://www.baeldung.com/java-google-cloud-storage
 */
@Slf4j
@Component
public class CloudStorageConsumer extends RabbitConsumer {

//    @Value("${}")
    private String credentialPath;

//    @Value("${}")
    private String projectId;

//    @Value("${}")
    private String bucketNamespace;

    private GoogleCredentials credential;
    private Storage cloudStorage;
    private Bucket bucket;

    private TodoService todoService;
    
    private AttachService attachService;

    @Autowired
    public CloudStorageConsumer(TodoService todoService, AttachService attachService) throws IOException {
//        credential = GoogleCredentials.fromStream(new FileInputStream(credentialPath));
//        cloudStorage = StorageOptions.newBuilder()
//                .setCredentials(credential)
//                .setProjectId(projectId)
//                .build()
//                .getService();
//        bucket = cloudStorage.create(BucketInfo.of(bucketNamespace));
        this.todoService = todoService;
        this.attachService = attachService;
    }

    /**
     * check database first
     * @param label
     * @param message
     * @param channel
     * @throws Exception
     */
    @Override
    protected void workerQueue(String label, Message message, Channel channel) throws Exception {
        try {
            byte[] receiveBytes = message.getBody();
            JSONObject payload = objectMapper.readValue(receiveBytes, JSONObject.class);
            if (hasAttachment(payload)) {
                return;
            }
            
            Blob blob = uploadAttach(payload);
            updateAttach(payload, blob.getMediaLink()); // TODO confirm blob.getMediaLink ??

        } catch (Exception e) {
            log.error("google cloud storage writing data ", e);

        } finally {
            //always ack
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
    
    private boolean hasAttachment(JSONObject payload) {
        return attachService.hasAttach(payload);
    }

    /**
     * 用 hash code 當 bucket 名稱才能唯一
     * @param payload
     * @return
     */
    private Blob uploadAttach(JSONObject payload) {
        String hash = payload.getString("hash");
        byte[] bytes = payload.getBytes("bytes");
        return bucket.create(hash, bytes);
    }

    private void updateAttach(JSONObject payload, String url) {
        Long tid = payload.getLong("tid");
        Integer partitionKey = payload.getInteger("partitionKey");
        String filename = payload.getString("filename");

        todoService.updateAttach(tid, partitionKey, filename, url);
    }

}
