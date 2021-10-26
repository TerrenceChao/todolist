package com.example.todolist.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;


@Slf4j
@Configuration
public class RabbitConfig {

    @Autowired
    private Environment env;

    @Autowired
    private CachingConnectionFactory connectionFactory;

    /**
     * 單一消費者
     * @return
     */
    @Bean(name = "singleListenerContainer")
    public SimpleRabbitListenerContainerFactory listenerContainer() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);

        // 設置併發消費者的初始數量：1
        factory.setConcurrentConsumers(1);

        // 設置併發消費者的最大數量：1
        factory.setMaxConcurrentConsumers(1);

        // 設置併發消費者中每個實例拉取的消息數量：1
        factory.setPrefetchCount(1);

        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        // 設置 發送消息後返回確認訊息
        connectionFactory.setPublisherReturns(true);

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMandatory(true);
        // 發送消息後，如果發送成功，則輸出“消息發送成功”的訊息
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                log.info("消息發送成功: correlationData({}), ack({}) cause({})", correlationData, ack, cause);
            }
        });

        // 發送消息後，如果發送失敗，則輸出“消息發送失敗-消息丟失”的訊息
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returnedMessage) {
                log.info("消息丟失: returnedMessage({})", returnedMessage);
            }
        });

        return rabbitTemplate;
    }




    /**
     * ==============================================================================
     *                                 Binding Queue
     * ==============================================================================
     */

    /**
     * for attachments uploading
     * @return
     */
    @Bean(name = "attachQueue")
    public Queue attachQueue() {
        return new Queue(env.getProperty("mq.attach.queue"), true);
    }

    @Bean
    public DirectExchange attachExchange() {
        return new DirectExchange(env.getProperty("mq.attach.exchange"), true, false);
    }

    @Bean
    public Binding attachBinding() {
        return BindingBuilder.bind(attachQueue())
                .to(attachExchange())
                .with(env.getProperty("mq.attach.routing.key"));
    }


    /**
     * for transformation from todo-task into todo-list
     * @return
     */
    @Bean(name = "transformQueue")
    public Queue transformQueue() {
        return new Queue(env.getProperty("mq.transform.queue"), true);
    }

    @Bean
    public DirectExchange transformExchange() {
        return new DirectExchange(env.getProperty("mq.transform.exchange"), true, false);
    }

    @Bean
    public Binding transformBinding() {
        return BindingBuilder.bind(transformQueue())
                .to(transformExchange())
                .with(env.getProperty("mq.transform.routing.key"));
    }
}
