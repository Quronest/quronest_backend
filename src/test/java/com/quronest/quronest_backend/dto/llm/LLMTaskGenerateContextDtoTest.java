package com.quronest.quronest_backend.dto.llm;

import com.quronest.quronest_backend.model.table.Job;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LLMTaskGenerateContextDtoTest {

    @Test
    void testSerializationAndDeserializationInJobMetadata() {
        UUID taskId = UUID.randomUUID();
        LLMTaskContextDto taskContext = new LLMTaskContextDto();
        taskContext.setTitle("Test Task Title");

        LLMUserContextDto userContext = new LLMUserContextDto();
        userContext.setCurrentStage("Stage 1");

        LLMDailyPlanContextDto planContext = new LLMDailyPlanContextDto("Plan Title", "Plan Desc", "Context");

        LLMTaskGenerateContextDto originalDto = new LLMTaskGenerateContextDto(taskId, taskContext, userContext, planContext);

        Job job = new Job();
        job.setMetadataAs(originalDto);

        LLMTaskGenerateContextDto deserializedDto = job.getMetadataAs(LLMTaskGenerateContextDto.class);

        assertNotNull(deserializedDto);
        assertEquals(taskId, deserializedDto.getTaskId());
        assertNotNull(deserializedDto.getTaskContext());
        assertEquals("Test Task Title", deserializedDto.getTaskContext().getTitle());
        assertNotNull(deserializedDto.getUserContext());
        assertEquals("Stage 1", deserializedDto.getUserContext().getCurrentStage());
        assertNotNull(deserializedDto.getPlanContext());
        assertEquals("Plan Title", deserializedDto.getPlanContext().getTitle());
        assertEquals("Plan Desc", deserializedDto.getPlanContext().getDescription());
        assertEquals("Context", deserializedDto.getPlanContext().getLlmContext());
    }
}
