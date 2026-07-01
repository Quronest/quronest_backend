package com.quronest.quronest_backend.service.rabbit;

import com.quronest.quronest_backend.config.RabbitConfig;
import com.quronest.quronest_backend.dto.ErrorMessageDto;
import com.quronest.quronest_backend.model.enums.JobStatus;
import com.quronest.quronest_backend.model.table.Job;
import com.quronest.quronest_backend.repository.JobRepository;
import com.quronest.quronest_backend.service.JobHandlerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class JobConsumerService {
    private static final Log log = LogFactory.getLog(JobConsumerService.class);
    private final JobHandlerService jobHandlerService;
    private final JobRepository jobRepository;
    private final JobProducerService jobProducerService;

    public JobConsumerService(JobHandlerService jobHandlerService,
                              JobRepository jobRepository, JobProducerService jobProducerService) {
        this.jobHandlerService = jobHandlerService;
        this.jobRepository = jobRepository;
        this.jobProducerService = jobProducerService;
    }

    @RabbitListener(queues = RabbitConfig.JOB_MAIN_QUEUE)
    public void consume(JobEvent event) {
        log.info("Received job: " + event.getJobId());
        Job job = jobRepository.findByIdAndStatus(event.getJobId(), JobStatus.PENDING);
        if (job == null) {
            return;
        }

        try {
            jobHandlerService.handleJob(job);

            // complete job
            job.markAsCompleted();
            jobRepository.save(job);

        } catch (Exception e) {
            log.error("Failed to complete job -  " + event.getJobId() + ": " + e);
            // use retry
            int retry = job.getRetries() + 1;
            job.setRetries(retry);

            // Job completely failed
            if (retry >= job.getMaxRetries()) {
                ErrorMessageDto errorMessageDto = new ErrorMessageDto(e.getMessage(), e.getClass().getSimpleName());
                job.setResultMetadataAs(errorMessageDto);
                job.markAsFailed();
                jobRepository.save(job);

                // send to DLQ
                jobProducerService.sendJobToDLQ(event);

            } else {
                // retry job
                job.setStatus(JobStatus.PENDING);
                jobRepository.save(job);

                // send to retry queue
                jobProducerService.sendJobToRetry(event);
            }
        }
    }
}
