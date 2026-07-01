package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.dto.JobUpdateEventDto;
import com.quronest.quronest_backend.model.enums.JobStatus;
import com.quronest.quronest_backend.model.table.Job;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WebSocketService {
    private static final Log log = LogFactory.getLog(WebSocketService.class);
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendJobUpdate(String username, JobUpdateEventDto payload) {
        try {
            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/job-updates",
                    payload
            );
        } catch (Exception e) {
            log.error("Failed to send job websocket event: " + e);
        }
    }

    public <T> void sendJobCompletedUpdate(Job job, T metadata){
        sendJobUpdate(job, JobStatus.COMPLETED, metadata);
    }

    public <T> void sendJobUpdate(Job job, T metadata) {
        sendJobUpdate(job, job.getStatus(), metadata);
    }

    public <T> void sendJobUpdate(Job job, JobStatus status, T metadata) {
        JobUpdateEventDto jobUpdateEventDto = new JobUpdateEventDto(job.getId(), status,
                                                                    metadata);

        sendJobUpdate(job.getUser().getEmail(), jobUpdateEventDto);
    }
}
