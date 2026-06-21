package com.quronest.quronest_backend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String JOB_QUEUE = "job.queue";
    public static final String JOB_EXCHANGE = "job.exchange";
    public static final String JOB_ROUTING_KEY = "job.routing";

    @Bean
    public Queue queue() {
        return QueueBuilder.durable(JOB_QUEUE).build();
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(JOB_EXCHANGE);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder
                .bind(queue())
                .to(exchange())
                .with(JOB_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
