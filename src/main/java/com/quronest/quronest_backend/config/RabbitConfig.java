package com.quronest.quronest_backend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.DefaultJacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String LLM_JOB_MAIN_QUEUE = "llm.job.main.queue";
    public static final String LLM_JOB_RESULT_QUEUE = "llm.job.result.queue";

    public static final String JOB_EXCHANGE = "job.exchange";

    @Bean
    public Queue llmJobMainQueue() {
        return QueueBuilder.durable(LLM_JOB_MAIN_QUEUE).build();
    }

    @Bean
    public Queue llmJobResultQueue() {
        return QueueBuilder.durable(LLM_JOB_RESULT_QUEUE).build();
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(JOB_EXCHANGE);
    }

    @Bean
    public Binding llmJobMainBinding() {
        return BindingBuilder.bind(llmJobMainQueue()).to(exchange()).with(LLM_JOB_MAIN_QUEUE);
    }

    @Bean
    public Binding llmJobResultBinding() {
        return BindingBuilder.bind(llmJobResultQueue()).to(exchange()).with(LLM_JOB_RESULT_QUEUE);
    }

    @Bean
    public MessageConverter messageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        DefaultJacksonJavaTypeMapper typeMapper = new DefaultJacksonJavaTypeMapper();
        typeMapper.setTypePrecedence(JacksonJavaTypeMapper.TypePrecedence.INFERRED);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}
