package com.quronest.quronest_backend.service.rabbit;

import com.quronest.quronest_backend.config.RabbitConfig;
import com.quronest.quronest_backend.dto.llm.LLMJobResultEventDto;
import com.quronest.quronest_backend.service.LLMJobResultHandlerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class JobConsumerService {
    private static final Log log = LogFactory.getLog(JobConsumerService.class);
    private final LLMJobResultHandlerService llmJobResultHandlerService;

    public JobConsumerService(LLMJobResultHandlerService llmJobResultHandlerService) {
        this.llmJobResultHandlerService = llmJobResultHandlerService;
    }

    @RabbitListener(queues = RabbitConfig.LLM_JOB_RESULT_QUEUE)
    public void consumeResult(LLMJobResultEventDto eventDto) {
        log.info("Received job result event: " + (eventDto != null ? eventDto.getEventId() : "null"));
        llmJobResultHandlerService.handleJobResult(eventDto);
    }
}
