package com.quronest.quronest_backend.model.table;

import com.fasterxml.jackson.databind.JsonNode;
import com.quronest.quronest_backend.model.enums.JobStatus;
import com.quronest.quronest_backend.model.enums.JobType;
import com.quronest.quronest_backend.utils.JsonUtils;
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
public class Job {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private JobType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private JsonNode metadata;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private JobStatus status = JobStatus.PENDING;

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

    public Job(User user, JobType type, JsonNode metadata) {
        this.user = user;
        this.type = type;
        this.metadata = metadata;
    }

    public <T> void setMetadataAs(T metadata) {
        this.metadata = JsonUtils.toJsonNode(metadata);
    }

    public <T> void setResultMetadataAs(T metadata) {
        this.resultMetadata = JsonUtils.toJsonNode(metadata);
    }

    public <T> T getMetadataAs(Class<T> clazz) {
        return JsonUtils.fromJsonNode(this.metadata, clazz);
    }

    public <T> T getResultMetadataAs(Class<T> clazz) {
        return JsonUtils.fromJsonNode(this.resultMetadata, clazz);
    }

    public void markAsCompleted() {
        this.status = JobStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = JobStatus.FAILED;
        this.failedAt = LocalDateTime.now();
    }
}
