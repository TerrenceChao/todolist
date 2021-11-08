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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Slf4j
@Component
@EnableAsync
public class LocalStorageConsumer extends BaseConsumer<JSONObject> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected JSONObject transformMsg(byte[] msgBody, long deliveryTag) throws Exception {
        log.info("本地儲存檔案 > transformMsg deliveryTag: {}", deliveryTag);
        return objectMapper.readValue(msgBody, JSONObject.class);
    }

    @Override
    protected void businessProcess(JSONObject payload, long deliveryTag) throws Exception {
        log.info("本地儲存檔案 > \ntid: {}, \nfilename: {}, \nhash: {}, \ndeliveryTag: {}\n", payload.getString("tid"), payload.getString("filename"), payload.getString("hash"), deliveryTag);

        String filename = payload.getString("filename");
        String fileOutput = "/Users/albert/Desktop/todo-images/" + filename;
        Path path = Paths.get(fileOutput);

        byte[] bytes = payload.getBytes("bytes");
        Files.write(path, bytes);
    }

//    @Async
//    @RabbitListener(
//            queues = "${mq.attach.queue}",
//            containerFactory = "singleListenerContainer"
//    )
    public void uploadAttach(@Payload Message message, Channel channel) throws Exception {
        consumeMessage(message, channel);
    }
}
