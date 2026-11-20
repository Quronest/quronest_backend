package com.quronest.quronest_backend.service.rabbit;

import com.quronest.quronest_backend.config.QuronestConfig;
import com.quronest.quronest_backend.config.RabbitConfig;
import com.quronest.quronest_backend.dto.llm.LLMJobEventDto;
import com.quronest.quronest_backend.model.enums.JobStatus;
import com.quronest.quronest_backend.model.enums.JobType;
import com.quronest.quronest_backend.model.table.Job;
import com.quronest.quronest_backend.model.table.User;
import com.quronest.quronest_backend.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class JobProducerServiceTest {

    private RabbitTemplate rabbitTemplate;
    private JobRepository jobRepository;
    private QuronestConfig quronestConfig;
    private JobProducerService jobProducerService;

    @BeforeEach
    void setUp() {
        rabbitTemplate = mock(RabbitTemplate.class);
        jobRepository = mock(JobRepository.class);
        quronestConfig = new QuronestConfig();
        jobProducerService = new JobProducerService(rabbitTemplate, jobRepository, quronestConfig);
    }

    @Test
    void publishLLMJob_shouldSetExpiryMinutesFromConfigAndExpiredAt() {
        quronestConfig.setLlmJobExpiryMinutes(45);

        User user = new User();
        user.setId(UUID.randomUUID());
        JobType jobType = JobType.LLM_GENERATE_USER_SUMMARY;
        Map<String, String> payload = Map.of("key", "value");

        LocalDateTime before = LocalDateTime.now().plusMinutes(44);

        Job result = jobProducerService.publishLLMJob(user, jobType, payload);

        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository, times(1)).save(jobCaptor.capture());

        Job savedJob = jobCaptor.getValue();
        assertEquals(45, savedJob.getExpiryMinutes());
        assertNotNull(savedJob.getExpiredAt());
        assertTrue(savedJob.getExpiredAt().isAfter(before));
        assertTrue(savedJob.getExpiredAt().isBefore(LocalDateTime.now().plusMinutes(46)));
        assertEquals(JobStatus.PENDING, savedJob.getStatus());

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitConfig.JOB_EXCHANGE),
                eq(RabbitConfig.LLM_JOB_MAIN_QUEUE),
                any(LLMJobEventDto.class)
        );

        assertSame(savedJob, result);
    }

    @Test
    void publishLLMJob_whenConfigDefault_shouldUse60Minutes() {
        User user = new User();
        user.setId(UUID.randomUUID());
        JobType jobType = JobType.LLM_GENERATE_DAILY_PLAN;
        Map<String, String> payload = Map.of("key", "value");

        Job result = jobProducerService.publishLLMJob(user, jobType, payload);

        assertEquals(60, result.getExpiryMinutes());
        assertNotNull(result.getExpiredAt());
    }
}
