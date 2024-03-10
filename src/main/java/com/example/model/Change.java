package com.example.model;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record Change(
        long timestamp,
        String entityId,
        String entityType
) {
}
