package com.example.todolist.config.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@EnableRabbit
//@Configuration
public class RabbitMqConfig extends BaseRabbitMqConfig<DirectExchange> {


//    @Bean
//    @Override
    public Queue queue() {
        return new Queue("cloud-storage-worker", true, false, false);
    }

//    @Bean
//    @Override
//    public DirectExchange exchange() {
//        return new DirectExchange("amqp.direct");
//    }
//
//    @Bean
//    @Override
//    public Binding binding() {
//        return BindingBuilder.bind(queue())
//                .to(exchange())
//                .with("amqp.direct");
//    }

//    @Bean
//    @Override
//    public MessageListenerAdapter listenerAdapter(MqListener listener) {
//        return new MessageListenerAdapter(listener, "receiveMessage");
//    }

}
