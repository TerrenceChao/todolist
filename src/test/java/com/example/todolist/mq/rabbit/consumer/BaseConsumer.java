package com.example.todolist.mq.rabbit.consumer;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * https://blog.csdn.net/qq_38082304/article/details/103049696
 */
@Slf4j
public class BaseConsumer {

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     *
     * @param label
     * @param message
     * @param channel
     * @throws Exception
     */
    protected void workerQueue(String label, Message message, Channel channel) throws Exception {
        try {
            byte[] receiveBytes = message.getBody();
            JSONObject payload = objectMapper.readValue(receiveBytes, JSONObject.class);
            System.out.println(label + " Received payload: " + payload);

        } catch (Exception e) {
            log.error("worker queue ", e);
        } finally {
            //always ack
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    /**
     * worker queue A
     * @param message
     * @param channel
     * @throws Exception
     */
    @RabbitListener(queuesToDeclare = @Queue(value = "cloud-storage-worker", durable = "true", exclusive = "false", autoDelete = "false"))
    public void workerQueueA(Message message, Channel channel) throws Exception {
        workerQueue("A)", message, channel);
    }

    /**
     * worker queue B
     * @param message
     * @param channel
     * @throws Exception
     */
    @RabbitListener(queuesToDeclare = @Queue(value = "cloud-storage-worker", durable = "true", exclusive = "false", autoDelete = "false"))
    public void workerQueueB(Message message, Channel channel) throws Exception {
        workerQueue("B)", message, channel);
    }


//    @RabbitListener(queues = {"cloud-storage-worker"})
//    @RabbitListener(bindings = {
//            @QueueBinding(
//                    value = @Queue, // 創建臨時queue
//                    exchange = @Exchange(
//                            value = "cloud-storage-worker",
//                            type = "fanout"
//                    )
//            )
//    })
//    public void receiveMessageA(Object payload) {
//        System.out.println("Received payload A: " + payload.toString());
//    }
//
//    @RabbitListener(bindings = {
//            @QueueBinding(
//                    value = @Queue, // 創建臨時queue
//                    exchange = @Exchange(
//                            value = "cloud-storage-worker",
//                            type = "fanout"
//                    )
//            )
//    })
//    public void receiveMessageB(Object payload) {
//        System.out.println("Received payload B: " + payload.toString());
//    }
}
