package com.ssafy.backend.batch.entity;

public enum OutboxStatus {
    READY,
    PROCESSING,
    COMPLETED,
    FAILED,
    READY_DELETE,
    READY_UPDATE,
}
