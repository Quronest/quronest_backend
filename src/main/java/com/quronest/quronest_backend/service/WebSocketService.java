package com.quronest.quronest_backend.service;

import com.quronest.quronest_backend.dto.JobUpdateEventDto;
import com.quronest.quronest_backend.model.enums.JobStatus;
import com.quronest.quronest_backend.model.table.Job;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendJobUpdate(String username, JobUpdateEventDto payload) {
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/job-updates",
                payload
        );
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
