package com.example.todolist.mq.rabbit;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StorageConsumer {

    @Autowired
    private ObjectMapper objectMapper;

//    @RabbitListener(
//            queues = "${mq.basic.queue}",
//            containerFactory = "singleListenerContainer"
//    )
//    public void uploadAttach(@Payload byte[] msg) {
//        try {
//            String message = new String(msg, "utf-8");
//            log.info("上傳附件-消費者-監聽消費：{}", message);
//        } catch (Exception e) {
//            log.error("上傳附件-消費者-發生異常：", e.fillInStackTrace());
//        }
//    }

//    @RabbitListener(
//            queues = "${mq.basic.queue}",
//            containerFactory = "singleListenerContainer"
//    )
    public void uploadAttach(@Payload Message message, Channel channel) throws Exception {

        MessageProperties messageProperties = message.getMessageProperties();
        // 獲取消息分發時的全局唯一標示
        long deliveryTag = messageProperties.getDeliveryTag();
        log.info("上傳附件-deliveryTag: {}", deliveryTag);

        try {
            JSONObject msg = objectMapper.readValue(message.getBody(), JSONObject.class);
            log.info("上傳附件-人為手動確認消費-監聽器監聽消費消息-內容為：{} ", msg);

            // TODO upload


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
