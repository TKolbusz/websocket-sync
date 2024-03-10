package com.example.model;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record Change(
        String tenantId,
        long timestamp,
        String entityId,
        String entityType
) {
}
