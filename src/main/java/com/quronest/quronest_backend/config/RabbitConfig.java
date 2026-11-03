package com.quronest.quronest_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
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
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}
