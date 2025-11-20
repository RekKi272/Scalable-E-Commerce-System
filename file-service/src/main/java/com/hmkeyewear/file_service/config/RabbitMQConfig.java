package com.hmkeyewear.file_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.file.queue}")
    private String fileQueueName;

    @Value("${app.rabbitmq.file.routing-key}")
    private String fileRoutingKey;

    @Bean
    public Queue fileQueue() {
        return new Queue(fileQueueName);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding fileBinding() {
        return BindingBuilder
                .bind(fileQueue())
                .to(exchange())
                .with(fileRoutingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // inject ConnectionFactory so RabbitTemplate is properly configured
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
