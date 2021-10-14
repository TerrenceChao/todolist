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
    public JSONObject transformMsg(byte[] msgBody) throws Exception {
        return objectMapper.readValue(msgBody, JSONObject.class);
    }

    @Override
    public void businessProcess(JSONObject payload) throws Exception {
        try {
            log.info("\n本地儲存檔案 \ntid: {}, \nfilename: {}, \nhash: {}\n", payload.getString("tid"), payload.getString("filename"), payload.getString("hash"));

            String filename = payload.getString("filename");
            String fileOutput = "/Users/albert/Desktop/todo-images/" + filename;
            Path path = Paths.get(fileOutput);

            byte[] bytes = payload.getBytes("bytes");
            Files.write(path, bytes);
        } catch (Exception e) {
            log.error("本地儲存檔案-人為手動確認消費-監聽器監聽消費消息-發生異常：", e.fillInStackTrace());
            throw e;
        }
    }

//    @Async
//    @RabbitListener(
//            queues = "${mq.basic.queue}",
//            containerFactory = "singleListenerContainer"
//    )
    public void uploadAttach(@Payload Message message, Channel channel) throws Exception {
        consumeMessage(message, channel);
    }
}
