package com.example.application;

import io.micronaut.serde.annotation.Serdeable;

import java.time.LocalDateTime;

@Serdeable
public record ReservationDTO(
        String id,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String customer
) {
}
