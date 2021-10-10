package com.example.todolist.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.todolist.service.AttachService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class AttachServiceImpl implements AttachService {

    @Autowired
    private Environment env;

    @Autowired
    public ObjectMapper objectMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public boolean hasAttach(JSONObject payload) {
        // TODO 從 google cloud storage 查詢是否有既存的 檔案
//        Long tid = payload.getLong("tid");
//        Integer partitionKey = payload.getInteger("partitionKey");
//        String filename = payload.getString("filename");
//        String hash = payload.getString("hash");
//        ...

        return true;
    }

    /**
     * @param tid
     * @param partitionKey weekOfYear
     * @param attachments
     * @param files
     */
    @Override
    public void uploadAttach(Long tid, Integer partitionKey, JSONObject attachments, List<MultipartFile> files) {
        log.info("tid:{}, \nattachments:{}, \nfiles:{} \nfile amount:{}", tid, attachments, files, files.size());
        files.forEach(file -> {
            JSONObject message = toMessage(tid, partitionKey, attachments, file);
            if (message.isEmpty()) {
                log.warn("生產者發送消息-沒有消息本體(null)");
            } else {
                sendMessage(message);
            }
        });
    }

    private void sendMessage(JSONObject msg) {
        try {
            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            rabbitTemplate.setExchange(env.getProperty("mq.basic.exchange"));
            rabbitTemplate.setRoutingKey(env.getProperty("mq.basic.routing.key"));

            Message message = MessageBuilder.withBody(objectMapper.writeValueAsBytes(msg))
                    .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                    .build();
            rabbitTemplate.convertAndSend(message);
//            log.info("生產者發送消息-內容為：{} ", msg);

        } catch (Exception e) {
            log.error("生產者發送消息-發生異常：{} ", msg, e.fillInStackTrace());
        }
    }

    private JSONObject toMessage(Long tid, Integer partitionKey, JSONObject attachments, MultipartFile file) {
        JSONObject fileMeta = attachments.getJSONObject(file.getOriginalFilename());
        JSONObject message = new JSONObject();

        try {
            message.put("tid", tid);
            message.put("partitionKey", partitionKey);
            message.put("hash", fileMeta.getString("hash"));
            message.put("contentType", file.getContentType());
            message.put("filename", file.getOriginalFilename());

            log.info("file's message tid:{} \nmessage:{}", tid, message);

            message.put("bytes", file.getBytes());
        } catch (IOException e) {
            message = new JSONObject();
            log.error("file bytes error ", e.getMessage());
        }

        return message;
    }
}
