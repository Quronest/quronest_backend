package com.quronest.quronest_backend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String JOB_MAIN_QUEUE = "job.main.queue";
    public static final String JOB_RETRY_QUEUE = "job.retry.queue";
    public static final String JOB_DLQ = "job.dlq";

    public static final String JOB_EXCHANGE = "job.exchange";

    @Bean
    public Queue jobMainQueue() {
        return QueueBuilder.durable(JOB_MAIN_QUEUE).build();
    }

    @Bean
    public Queue jobRetryQueue() {
        return QueueBuilder.durable(JOB_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", JOB_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", JOB_MAIN_QUEUE)
                .withArgument("x-message-ttl", 10000) // 10 sec delay
                .build();
    }

    // DLQ
    @Bean
    public Queue jobDeadLetterQueue() {
        return QueueBuilder.durable(JOB_DLQ).build();
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(JOB_EXCHANGE);
    }

    @Bean
    public Binding mainBinding() {
        return BindingBuilder.bind(jobMainQueue()).to(exchange()).with(JOB_MAIN_QUEUE);
    }

    @Bean
    public Binding retryBinding() {
        return BindingBuilder.bind(jobRetryQueue()).to(exchange()).with(JOB_RETRY_QUEUE);
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(jobDeadLetterQueue()).to(exchange()).with(JOB_DLQ);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
