package com.quronest.quronest_backend.dto.llm;

import com.quronest.quronest_backend.model.table.Job;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DailyPlanGenerateLLMRequestDtoTest {

    @Test
    void testSerializationAndDeserializationInJobMetadata() {
        LocalDate startDate = LocalDate.of(2026, 11, 20);
        LLMUserContextDto userContext = new LLMUserContextDto();
        userContext.setCurrentStage("Stage 2");

        DailyPlanGenerateLLMRequestDto originalDto = new DailyPlanGenerateLLMRequestDto(userContext, startDate, 8);

        Job job = new Job();
        job.setMetadataAs(originalDto);

        DailyPlanGenerateLLMRequestDto deserializedDto = job.getMetadataAs(DailyPlanGenerateLLMRequestDto.class);

        assertNotNull(deserializedDto);
        assertEquals(startDate, deserializedDto.getStartDate());
        assertEquals(8, deserializedDto.getLimit());
        assertNotNull(deserializedDto.getUserContext());
        assertEquals("Stage 2", deserializedDto.getUserContext().getCurrentStage());
    }
}
