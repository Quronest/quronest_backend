package com.quronest.quronest_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class ReadingTask {
    @JsonProperty("content_markdown")
    private String contentMarkdown;

    @JsonProperty("sources")
    private List<SourceUrl> sources;

    @JsonProperty("youtube_video_url")
    private String youtubeVideoUrl;

    @JsonProperty("youtube_video_summary")
    private String youtubeVideoSummary;
}
