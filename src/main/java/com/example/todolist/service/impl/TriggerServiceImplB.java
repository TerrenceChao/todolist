package com.example.todolist.service.impl;


import com.example.todolist.service.TriggerService;
import com.example.todolist.util.ByteUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;


@Slf4j
@Service("triggerServiceB")
public class TriggerServiceImplB implements TriggerService {

    @Autowired
    private RedissonClient redisson;

    @Autowired
    @Qualifier("rabbitTemplate")
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ByteUtil byteUtil;

    @Value("${todo-task.count}")
    private String taskCountKey;

    @Value("${todo-task.first-timestamp}")
    private String taskFirstTimestampKey;

    @Value("${todo-task.last-timestamp}")
    private String taskLastTimestampKey;

    @Value("${mq.transform.exchange}")
    private String mqExchange;

    @Value("${mq.transform.routing.key}")
    private String mqRoutingKey;

    private long max;

    public TriggerServiceImplB(RedissonClient redisson, RabbitTemplate rabbitTemplate, ByteUtil byteUtil, Environment env) {
        this.redisson = redisson;
        this.rabbitTemplate = rabbitTemplate;
        this.byteUtil = byteUtil;

        max = Long.parseLong(Objects.requireNonNullElse(env.getProperty("todo-task.max"), "20"));
    }

    @Override
    public void transformAsync(Long timestamp) {
        sendMessage(timestamp);
//        log.info("Trigger B) transformAsync");
    }

    @Override
    public long transform(Long currentTimestamp) {
        RAtomicLong taskCnt = redisson.getAtomicLong(taskCountKey);
        if (taskCnt.addAndGet(1L) % max == 1) {
            // previousTime = firstTimestamp of previous round
            RAtomicLong previousTime = redisson.getAtomicLong(taskFirstTimestampKey);
            long previousTimestamp = previousTime.get();
            // currentTimestamp = firstTimestamp of current round
            previousTime.set(currentTimestamp);
            return previousTimestamp;
        }

        return -1L;
    }

    @Override
    public long getLastTimestamp() {
        return redisson.getAtomicLong(taskLastTimestampKey).get();
    }

    @Override
    public void setLastTimestamp(long timestamp) {
        RAtomicLong rAtomicLong = redisson.getAtomicLong(taskLastTimestampKey);
        rAtomicLong.set(timestamp);
    }

    private void sendMessage(Long msg) {
        if (Objects.isNull(msg)) {
            log.info("生產者發送消息-內容為空");
            return;
        }

        String messageId = UUID.randomUUID().toString();
        try {
            Message message = MessageBuilder.withBody(byteUtil.longToBytes(msg))
                    .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                    .build();

            rabbitTemplate.convertAndSend(
                    mqExchange,
                    mqRoutingKey,
                    message,
                    new CorrelationData(messageId)
            );
            log.info("B) 生產者發送消息-傳送資訊 messageId: {}，message: {}, exchange: {}, routingKey: {}",
                    messageId,
                    msg,
                    mqExchange,
                    mqRoutingKey
            );

        } catch (Exception e) {
            log.error("B) 生產者發送消息-發生異常 messageId: {}，message: {}, exchange: {}, routingKey: {}",
                    messageId,
                    msg,
                    mqExchange,
                    mqRoutingKey,
                    e.fillInStackTrace()
            );
        }
    }
}
