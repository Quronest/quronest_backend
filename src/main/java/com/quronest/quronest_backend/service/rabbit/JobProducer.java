package com.quronest.quronest_backend.service.rabbit;

import com.quronest.quronest_backend.config.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class JobProducer {
    private final RabbitTemplate rabbitTemplate;

    public JobProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(JobEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.JOB_EXCHANGE,
                RabbitConfig.JOB_ROUTING_KEY,
                event
        );
    }
}
