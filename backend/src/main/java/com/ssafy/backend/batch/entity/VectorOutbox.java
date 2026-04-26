package com.ssafy.backend.batch.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(
        name = "vector_outbox",
        indexes = {
                @Index(name = "idx_vector_outbox_status_created_at", columnList = "status, created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VectorOutbox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "house_id", nullable = false)
    private Long houseId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "content", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(length = 20)
    private OutboxStatus status = OutboxStatus.READY;

    @Builder.Default
    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.retryCount == null) this.retryCount = 0;
        if (this.status == null) this.status = OutboxStatus.READY;
    }

    // --- 비즈니스 로직 메서드 ---

    public void markProcessing() {
        this.status = OutboxStatus.PROCESSING;
    }

    public void complete() {
        this.status = OutboxStatus.COMPLETED;
    }

    public void fail() {
        this.status = OutboxStatus.FAILED;
        this.retryCount++;
    }

    public void retry() {
        this.status = OutboxStatus.READY;
        this.retryCount++;
    }

    public void markReadyDelete() {
        this.status = OutboxStatus.READY_DELETE;
        this.retryCount = 0;
    }

    public void updateData(Map<String, Object> content, Map<String, Object> metadata) {
        this.content = content;
        this.metadata = metadata;
        this.status = OutboxStatus.READY_UPDATE;
        this.retryCount = 0;
    }
}
