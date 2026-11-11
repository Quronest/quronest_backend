package com.quronest.quronest_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ReadingTaskContent {
    @JsonProperty("markdown_content")
    private String markdownContent;

    @JsonProperty("sources")
    private List<SourceUrl> sources;

    @JsonProperty("youtube_video_url")
    private String youtubeVideoUrl;

    @JsonProperty("youtube_video_summary")
    private String youtubeVideoSummary;

    @JsonProperty("questionnaires")
    private List<QuizQuestion> questionnaires;

    @JsonProperty("max_total_point")
    private Integer maxTotalPoint;

    public ReadingTaskContent(String markdownContent, List<SourceUrl> sources, String youtubeVideoUrl,
                              String youtubeVideoSummary, List<QuizQuestion> questionnaires) {
        this.markdownContent = markdownContent;
        this.sources = sources;
        this.youtubeVideoUrl = youtubeVideoUrl;
        this.youtubeVideoSummary = youtubeVideoSummary;
        this.questionnaires = questionnaires;
    }
}
