package com.quronest.quronest_backend.repository;

import com.quronest.quronest_backend.TestcontainersConfiguration;
import com.quronest.quronest_backend.model.enums.JobStatus;
import com.quronest.quronest_backend.model.enums.JobType;
import com.quronest.quronest_backend.model.table.Job;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "spring.liquibase.enabled=true")
@Transactional
class JobRepositoryIntegrationTest {

    @Autowired
    private JobRepository jobRepository;

    @Test
    void updateStatusForExpiredJobs_shouldOnlyFailPendingExpiredJobs() {
        LocalDateTime now = LocalDateTime.now();

        Job expiredPending = new Job();
        expiredPending.setType(JobType.LLM_GENERATE_USER_SUMMARY);
        expiredPending.setStatus(JobStatus.PENDING);
        expiredPending.setExpiryMinutes(60);
        expiredPending.setExpiredAt(now.minusMinutes(10));
        expiredPending = jobRepository.save(expiredPending);

        Job activePending = new Job();
        activePending.setType(JobType.LLM_GENERATE_USER_SUMMARY);
        activePending.setStatus(JobStatus.PENDING);
        activePending.setExpiryMinutes(60);
        activePending.setExpiredAt(now.plusMinutes(50));
        activePending = jobRepository.save(activePending);

        Job expiredCompleted = new Job();
        expiredCompleted.setType(JobType.LLM_GENERATE_USER_SUMMARY);
        expiredCompleted.setStatus(JobStatus.COMPLETED);
        expiredCompleted.setExpiryMinutes(60);
        expiredCompleted.setExpiredAt(now.minusMinutes(20));
        expiredCompleted = jobRepository.save(expiredCompleted);

        Job noExpiryJob = new Job();
        noExpiryJob.setType(JobType.LLM_GENERATE_USER_SUMMARY);
        noExpiryJob.setStatus(JobStatus.PENDING);
        noExpiryJob = jobRepository.save(noExpiryJob);

        int updatedCount = jobRepository.updateStatusForExpiredJobs(JobStatus.PENDING, JobStatus.FAILED, now);
        assertEquals(1, updatedCount);

        Job updatedExpired = jobRepository.findById(expiredPending.getId()).orElseThrow();
        assertEquals(JobStatus.FAILED, updatedExpired.getStatus());
        assertNotNull(updatedExpired.getFailedAt());

        Job updatedActive = jobRepository.findById(activePending.getId()).orElseThrow();
        assertEquals(JobStatus.PENDING, updatedActive.getStatus());
        assertNull(updatedActive.getFailedAt());

        Job updatedCompleted = jobRepository.findById(expiredCompleted.getId()).orElseThrow();
        assertEquals(JobStatus.COMPLETED, updatedCompleted.getStatus());

        Job updatedNoExpiry = jobRepository.findById(noExpiryJob.getId()).orElseThrow();
        assertEquals(JobStatus.PENDING, updatedNoExpiry.getStatus());
    }
}
