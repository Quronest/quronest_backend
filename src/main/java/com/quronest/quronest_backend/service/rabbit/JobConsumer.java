package com.quronest.quronest_backend.service.rabbit;

import com.quronest.quronest_backend.config.RabbitConfig;
import com.quronest.quronest_backend.service.JobHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class JobConsumer {
    private static final Log log = LogFactory.getLog(JobConsumer.class);
    private final JobHandler jobHandler;

    public JobConsumer(JobHandler jobHandler) {
        this.jobHandler = jobHandler;
    }

    @RabbitListener(queues = RabbitConfig.JOB_QUEUE)
    public void consume(JobEvent event) {
        log.info("Received job: " + event.getJobId());

        jobHandler.handle(event.getJobId());
    }
}
