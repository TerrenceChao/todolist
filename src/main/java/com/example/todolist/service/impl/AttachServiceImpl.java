package com.example.todolist.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.example.todolist.db.rmdb.entity.Attachment;
import com.example.todolist.db.rmdb.repo.AttachmentRepository;
import com.example.todolist.service.AttachService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class AttachServiceImpl implements AttachService {

    @Autowired
    private Environment env;

    @Autowired
    public ObjectMapper objectMapper;

    @Autowired
    @Qualifier("rabbitTemplateJSONConverter")
    private RabbitTemplate rabbitTemplateJSONConverter;

    @Autowired
    private AttachmentRepository attachRepo;

    /**
     * @param tid
     * @param partitionKey weekOfYear
     * @param attachments
     * @param files
     */
    @Override
    public void uploadAttach(Long tid, Integer partitionKey, JSONObject attachments, List<MultipartFile> files) {
        log.info("tid:{}, \nattachments:{}, \nfiles:{} \nfile amount:{}", tid, attachments, files, files.size());
        for (MultipartFile file : files) {
            JSONObject message = toMessage(tid, partitionKey, attachments, file);
            if (message.isEmpty()) {
                log.warn("生產者發送消息-沒有消息本體(null)");
                continue;
            }

            String hashcode = message.getString("hash");
            Attachment attachInfo = attachRepo.findById(hashcode);
            if (Objects.nonNull(attachInfo)) {
                log.info("該檔案已存在 attach info: {}", attachInfo);
                continue;
            }

            sendMessage(message);
        };
    }

    private void sendMessage(JSONObject msg) {
        String messageId = UUID.randomUUID().toString();
        try {
            String mqExchange = env.getProperty("mq.attach.exchange");
            String mqRoutingKey = env.getProperty("mq.attach.routing.key");

            Message message = MessageBuilder.withBody(objectMapper.writeValueAsBytes(msg))
                    .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                    .build();

            // for log print before 'convertAndSend'
            msg.remove("bytes");

            rabbitTemplateJSONConverter.convertAndSend(
                    mqExchange,
                    mqRoutingKey,
                    message,
                    new CorrelationData(messageId)
            );

            log.info("生產者發送消息-傳送資訊 messageId: {}，message: {}, exchange: {}, routingKey: {}",
                    messageId,
                    msg,
                    mqExchange,
                    mqRoutingKey
            );

        } catch (Exception e) {
            log.error("生產者發送消息-發生異常 messageId: {}，message: {}",
                    messageId,
                    msg,
                    e.fillInStackTrace()
            );
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
            log.error("file bytes error ", e.getStackTrace());
        }

        return message;
    }
}
