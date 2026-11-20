package com.quronest.quronest_backend.service.rabbit;

import com.fasterxml.jackson.databind.JsonNode;
import com.quronest.quronest_backend.config.QuronestConfig;
import com.quronest.quronest_backend.config.RabbitConfig;
import com.quronest.quronest_backend.dto.llm.LLMJobDto;
import com.quronest.quronest_backend.dto.llm.LLMJobEventDto;
import com.quronest.quronest_backend.exception.JobAlreadyExistsException;
import com.quronest.quronest_backend.model.enums.JobStatus;
import com.quronest.quronest_backend.model.enums.JobType;
import com.quronest.quronest_backend.model.table.Job;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.JobRepository;
import com.quronest.quronest_backend.utils.JsonUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class JobProducerService {
    private static final int DEFAULT_LLM_JOB_EXPIRY_MINUTES = 60;

    private final RabbitTemplate rabbitTemplate;
    private final JobRepository jobRepository;
    private final QuronestConfig quronestConfig;

    public JobProducerService(RabbitTemplate rabbitTemplate,
                              JobRepository jobRepository,
                              QuronestConfig quronestConfig) {
        this.rabbitTemplate = rabbitTemplate;
        this.jobRepository = jobRepository;
        this.quronestConfig = quronestConfig;
    }

    public <T> void publish(String queueName, T event) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.JOB_EXCHANGE,
                queueName,
                event
        );
    }

    public <T> Job publishLLMJob(User user, JobType type, T payload) {
        JsonNode metadata = JsonUtils.toJsonNode(payload);
        int expiryMinutes = getLlmJobExpiryMinutes();
        Job job = new Job(user, type, metadata, expiryMinutes);
        jobRepository.save(job);

        LLMJobDto jobDto = new LLMJobDto(job.getId(), type, metadata, job.getMaxRetries());
        LLMJobEventDto eventDto = new LLMJobEventDto(UUID.randomUUID(), jobDto, LocalDateTime.now(), 0);
        publish(RabbitConfig.LLM_JOB_MAIN_QUEUE, eventDto);

        return job;
    }

    private int getLlmJobExpiryMinutes() {
        if (quronestConfig != null && quronestConfig.getLlmJobExpiryMinutes() != null) {
            return quronestConfig.getLlmJobExpiryMinutes();
        }
        return DEFAULT_LLM_JOB_EXPIRY_MINUTES;
    }

    public Job verifyAndGetExistedJob(User user, JobType type) {
        List<JobStatus> checkStatus = List.of(JobStatus.PENDING, JobStatus.LOCKED);
        return jobRepository.findByUserAndTypeAndStatusIn(user, type, checkStatus);
    }

    public void verifyExistedJobAndThrow(User user, JobType type, String message) {
        Job job = verifyAndGetExistedJob(user, type);
        if (job != null) {
            throw new JobAlreadyExistsException(message);
        }
    }
}
