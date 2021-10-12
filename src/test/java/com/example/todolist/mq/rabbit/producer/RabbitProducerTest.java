package com.example.todolist.mq.rabbit.producer;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Slf4j
@Component
public class RabbitProducerTest implements RabbitTemplate.ConfirmCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {
        // TODO
        log.info("MQ ack confirm.  correlationData:{} b:{}, s:{}", correlationData, b, s);
    }

    public void sendMessage(String routingKey, Object message) {
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());

        String messageId = UUID.randomUUID().toString();

        try {
            MessageProperties properties = new MessageProperties();
            properties.setMessageId(messageId);
            properties.setDeliveryTag(System.currentTimeMillis());

            rabbitTemplate.convertAndSend(
                    routingKey,
                    MessageBuilder.withBody(objectMapper.writeValueAsBytes(message)).andProperties(properties).build(),
                    new CorrelationData(messageId)
            );

            log.info("MQ send success, routingKey: {}, messageId: {}, message: {}",
                    routingKey,
                    messageId,
                    JSON.toJSONString(message)
            );

        } catch (Exception e) {
            log.error("MQ send fail, routingKey: {}, messageId: {}, message: {}, ex: {}",
                    routingKey,
                    messageId,
                    JSON.toJSONString(message),
                    e.getMessage()
            );
        }
    }

    public void sendMessage(String exchange, String routingKey, Object message) {
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());

        String messageId = UUID.randomUUID().toString();

        try {
            MessageProperties properties = new MessageProperties();
            properties.setMessageId(messageId);
            properties.setDeliveryTag(System.currentTimeMillis());

//            properties.setHeader(MdcConstant.TRACEID, MDC.get(MdcConstant.TRACEID));
//            properties.setHeader(MdcConstant.ACTION , MDC.get(MdcConstant.ACTION));

            log.info("MQ send success, messageId: {}，message: {}, exchange: {}, routingKey: {}",
                    messageId,
                    JSON.toJSONString(message),
                    exchange,
                    routingKey
            );

            rabbitTemplate.convertAndSend(
                    exchange,
                    routingKey,
                    MessageBuilder.withBody(objectMapper.writeValueAsBytes(message)).andProperties(properties).build(),
                    new CorrelationData(messageId)
            );
        } catch (Exception e) {
            log.error("MQ send fail, messageId: {}，message: {}, exchange: {}, routingKey: {}, ex: {}",
                    messageId,
                    JSON.toJSONString(message),
                    exchange,
                    routingKey,
                    e.getMessage()
            );
        }
    }
}
