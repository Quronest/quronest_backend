package com.quronest.quronest_backend.jobs;

import com.quronest.quronest_backend.model.enums.JobStatus;
import com.quronest.quronest_backend.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class JobExpirySchedulerTest {

    private JobRepository jobRepository;
    private JobExpiryScheduler jobExpiryScheduler;

    @BeforeEach
    void setUp() {
        jobRepository = mock(JobRepository.class);
        jobExpiryScheduler = new JobExpiryScheduler(jobRepository);
    }

    @Test
    void markExpiredJobsAsFailed_shouldCallRepositoryWithPendingAndFailedStatus() {
        when(jobRepository.updateStatusForExpiredJobs(eq(JobStatus.PENDING), eq(JobStatus.FAILED), any(LocalDateTime.class)))
                .thenReturn(3);

        jobExpiryScheduler.markExpiredJobsAsFailed();

        ArgumentCaptor<LocalDateTime> timeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(jobRepository, times(1)).updateStatusForExpiredJobs(
                eq(JobStatus.PENDING),
                eq(JobStatus.FAILED),
                timeCaptor.capture()
        );
        assertNotNull(timeCaptor.getValue());
    }

    @Test
    void markExpiredJobsAsFailed_whenNoJobsExpired_shouldExecuteWithoutError() {
        when(jobRepository.updateStatusForExpiredJobs(eq(JobStatus.PENDING), eq(JobStatus.FAILED), any(LocalDateTime.class)))
                .thenReturn(0);

        jobExpiryScheduler.markExpiredJobsAsFailed();

        verify(jobRepository, times(1)).updateStatusForExpiredJobs(
                eq(JobStatus.PENDING),
                eq(JobStatus.FAILED),
                any(LocalDateTime.class)
        );
    }
}
