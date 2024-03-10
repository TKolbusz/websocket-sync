package com.example.model;

import java.time.LocalDateTime;

public record Reservation(
        String id,
        String tenantId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String customer
) {
}
