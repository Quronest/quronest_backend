package com.quronest.quronest_backend.jobs;

import com.quronest.quronest_backend.model.enums.JobStatus;
import com.quronest.quronest_backend.repository.JobRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class JobExpiryScheduler {
    private static final Log log = LogFactory.getLog(JobExpiryScheduler.class);

    private final JobRepository jobRepository;

    public JobExpiryScheduler(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void markExpiredJobsAsFailed() {
        LocalDateTime now = LocalDateTime.now();
        int updatedCount = jobRepository.updateStatusForExpiredJobs(JobStatus.PENDING, JobStatus.FAILED, now);
        if (updatedCount > 0) {
            log.info("Marked " + updatedCount + " expired PENDING job(s) as FAILED at " + now);
        }
    }
}
