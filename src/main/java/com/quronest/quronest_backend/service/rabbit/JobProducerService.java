package com.quronest.quronest_backend.service.rabbit;

import com.quronest.quronest_backend.config.RabbitConfig;
import com.quronest.quronest_backend.model.enums.JobType;
import com.quronest.quronest_backend.model.table.Job;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.JobRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class JobProducerService {
    private final RabbitTemplate rabbitTemplate;
    private final JobRepository jobRepository;

    public JobProducerService(RabbitTemplate rabbitTemplate,
                              JobRepository jobRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.jobRepository = jobRepository;
    }

    public void sendNewJob(JobEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.JOB_EXCHANGE,
                RabbitConfig.JOB_MAIN_QUEUE,
                event
        );
    }

    public void sendJobToRetry(JobEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.JOB_EXCHANGE,
                RabbitConfig.JOB_RETRY_QUEUE,
                event
        );
    }

    public void sendJobToDLQ(JobEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.JOB_EXCHANGE,
                RabbitConfig.JOB_DLQ,
                event
        );
    }

    public <T> Job createAndSendNewJob(User user, JobType type, T payload) {
        Job job = new Job(user, type, payload);
        jobRepository.save(job);

        sendNewJob(new JobEvent(job.getId(), "LLM_JOB"));

        return job;
    }
}
