package com.example.todolist.service.impl;

import com.example.todolist.service.TriggerService;
import com.example.todolist.util.ByteUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Objects;


@Slf4j
@Primary
@Service("triggerServiceA")
public class TriggerServiceImplA implements TriggerService {

    @Autowired
    private RedissonClient redisson;

    @Autowired
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

    public TriggerServiceImplA(RedissonClient redisson, RabbitTemplate rabbitTemplate, ByteUtil byteUtil, Environment env) {
        this.redisson = redisson;
        this.rabbitTemplate = rabbitTemplate;
        this.byteUtil = byteUtil;

        max = Long.parseLong(Objects.requireNonNullElse(env.getProperty("todo-task.max"), "20"));
    }

    @Override
    public void transformAsync(Long currentTimestamp) {
        // TODO 用 Redisson 是否過慢!?
        RAtomicLong taskCnt = redisson.getAtomicLong(taskCountKey);
        if (taskCnt.addAndGet(1L) % max == 1) {
            // previousTime = firstTimestamp of previous round
            RAtomicLong previousTime = redisson.getAtomicLong(taskFirstTimestampKey);
            sendMessage(previousTime.get());
            // currentTimestamp = firstTimestamp of current round
            previousTime.set(currentTimestamp);
        }
//        log.info("Trigger A) transformAsync");
    }

    /**
     * do nothing in TriggerServiceA (TriggerServiceImplA)
     * @param timestamp
     * @return
     */
    @Override
    public long transform(Long timestamp) {
        return 0;
    }

    /**
     * todo-list 最後一行的 next_created_at
     * @return
     */
    @Override
    public long getLastTimestamp() {
        // TODO 用 Redisson 是否過慢!?
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

        try {
            rabbitTemplate.setExchange(mqExchange);
            rabbitTemplate.setRoutingKey(mqRoutingKey);

            Message message = MessageBuilder.withBody(byteUtil.longToBytes(msg))
                    .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                    .build();
            rabbitTemplate.convertAndSend(message);
            log.info("生產者發送消息-內容為：{} ", msg);

        } catch (Exception e) {
            log.error("生產者發送消息-發生異常：{} ", msg, e.fillInStackTrace());
        }
    }
}
