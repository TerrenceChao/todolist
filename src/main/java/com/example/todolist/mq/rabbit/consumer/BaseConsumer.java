package com.example.todolist.mq.rabbit.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.messaging.handler.annotation.Payload;


@Slf4j
public abstract class BaseConsumer<Msg> {

    protected abstract Msg transformMsg(byte[] msgBody) throws Exception;

    protected abstract void businessProcess(Msg payload) throws Exception;

    protected void consumeMessage(@Payload Message message, Channel channel) throws Exception {

        MessageProperties messageProperties = message.getMessageProperties();
        // 獲取消息分發時的全局唯一標示
        long deliveryTag = messageProperties.getDeliveryTag();
        log.info("上傳附件-deliveryTag: {}", deliveryTag);

        try {
            Msg msg = transformMsg(message.getBody());
            // log.info("上傳附件-人為手動確認消費-監聽器監聽消費消息-內容為：{} ", msg);

            // do business process
            businessProcess(msg);

            //第一个参数为：消息的分发标识(唯一);第二个参数：是否允许批量确认消费(在这里设置为true)
            channel.basicAck(deliveryTag, true);

        } catch (Exception e) {
            log.error("上傳附件-人為手動確認消費-監聽器監聽消費消息-發生異常：", e.fillInStackTrace());

            //如果在处理消息的过程中发生了异常,则照样需要人为手动确认消费掉该消息
            // (否则该消息将一直留在队列中,从而将导致消息的重复消费)
            channel.basicReject(deliveryTag, false);
        }
    }
}
