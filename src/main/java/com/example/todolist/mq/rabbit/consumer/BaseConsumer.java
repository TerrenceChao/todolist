package com.example.todolist.mq.rabbit.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.messaging.handler.annotation.Payload;


@Slf4j
public abstract class BaseConsumer<Msg> {

    protected abstract Msg transformMsg(byte[] msgBody, long deliveryTag) throws Exception;

    protected abstract void businessProcess(Msg payload, long deliveryTag) throws Exception;

    protected void consumeMessage(@Payload Message message, Channel channel) throws Exception {

        MessageProperties messageProperties = message.getMessageProperties();
        // 獲取消息分發時的全局唯一標示
        long deliveryTag = messageProperties.getDeliveryTag();
        log.info("rabbitmq deliveryTag: {}", deliveryTag);

        try {
            Msg msg = transformMsg(message.getBody(), deliveryTag);

            // do business process
            businessProcess(msg, deliveryTag);

            //第一个参数为：消息的分发标识(唯一);第二个参数：是否允许批量确认消费(在这里设置为true)
            channel.basicAck(deliveryTag, true);
            log.info("channel ack. deliveryTag: {}", deliveryTag);

        } catch (Exception e) {
            //如果在处理消息的过程中发生了异常,则照样需要人为手动确认消费掉该消息
            // (否则该消息将一直留在队列中,从而将导致消息的重复消费)
            channel.basicReject(deliveryTag, false);
            log.error("channel REJECT. deliveryTag: {}", deliveryTag, e.fillInStackTrace());
        }

    }
}
