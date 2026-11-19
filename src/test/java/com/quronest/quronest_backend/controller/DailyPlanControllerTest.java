package com.quronest.quronest_backend.controller;

import com.quronest.quronest_backend.dto.JobStatusDto;
import com.quronest.quronest_backend.model.enums.JobStatus;
import com.quronest.quronest_backend.model.enums.JobType;
import com.quronest.quronest_backend.model.table.Job;
import com.quronest.quronest_backend.service.DailyPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyPlanControllerTest {

    @Mock
    private DailyPlanService dailyPlanService;

    @InjectMocks
    private DailyPlanController dailyPlanController;

    private JobStatusDto jobStatusDto;

    @BeforeEach
    void setUp() {
        Job job = new Job();
        job.setId(UUID.randomUUID());
        job.setType(JobType.LLM_GENERATE_DAILY_PLAN);
        job.setStatus(JobStatus.PENDING);
        jobStatusDto = new JobStatusDto(job);
    }

    @Test
    void generateNewDailyPlans_delegatesToServiceWithParams() {
        LocalDate startDate = LocalDate.of(2026, 11, 20);
        int limit = 7;

        when(dailyPlanService.generateNextPlans(startDate, limit)).thenReturn(jobStatusDto);

        JobStatusDto result = dailyPlanController.generateNewDailyPlans(startDate, limit);

        assertThat(result).isNotNull();
        assertThat(result.getJobId()).isEqualTo(jobStatusDto.getJobId());
        verify(dailyPlanService).generateNextPlans(startDate, limit);
    }

    @Test
    void generateNewDailyPlans_delegatesWithNullParams() {
        when(dailyPlanService.generateNextPlans(null, null)).thenReturn(jobStatusDto);

        JobStatusDto result = dailyPlanController.generateNewDailyPlans(null, null);

        assertThat(result).isNotNull();
        verify(dailyPlanService).generateNextPlans(null, null);
    }
}
