package com.example.todolist.config.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

public abstract class BaseRabbitMqConfig<E extends AbstractExchange> {

    protected String queue;

    protected String exchange;

    protected String routingKey;

    protected String username;

    protected String password;

    protected String host;

    public abstract Queue queue();

//    public abstract E exchange();
//
//    public abstract Binding binding();

    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }



//    public abstract MessageListenerAdapter listenerAdapter(MqListener listener);


//    public abstract ConnectionFactory connectionFactory();
//
//    public abstract MessageConverter jsonMessageConverter();

//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory factory) {
//        final RabbitTemplate rabbitTemplate = new RabbitTemplate(factory);
//        rabbitTemplate.setMessageConverter(jsonMessageConverter());
//        return rabbitTemplate;
//    }
}
