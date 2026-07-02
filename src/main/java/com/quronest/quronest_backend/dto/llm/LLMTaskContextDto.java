package com.quronest.quronest_backend.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quronest.quronest_backend.model.enums.DailyTaskLevel;
import com.quronest.quronest_backend.model.enums.Domain;
import com.quronest.quronest_backend.model.table.DailyTask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LLMTaskContextDto {
    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("domain")
    private Domain domain;

    @JsonProperty("subdomains")
    private List<String> subdomains = new ArrayList<>();

    @JsonProperty("tags")
    private List<String> tags = new ArrayList<>();

    @JsonProperty("level")
    private DailyTaskLevel level;

    public LLMTaskContextDto(DailyTask task) {
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.domain = task.getDomain();
        this.subdomains = task.getSubdomains();
        this.tags = task.getTags();
        this.level = task.getLevel();
    }
}
