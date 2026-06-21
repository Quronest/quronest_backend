package com.quronest.quronest_backend.model.table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.quronest.quronest_backend.model.enums.JobStatus;
import com.quronest.quronest_backend.model.enums.JobType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Job {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private JobType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private JsonNode metadata;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private JobStatus status;

    @Column(name = "retries")
    private Integer retries = 0;

    @Column(name = "max_retries")
    private Integer maxRetries = 1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_metadata", columnDefinition = "jsonb")
    private JsonNode resultMetadata;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "creation_timestamp")
    @CreationTimestamp
    private LocalDateTime creationTimestamp;

    @Column(name = "update_timestamp")
    @UpdateTimestamp
    private LocalDateTime updateTimestamp;

}
