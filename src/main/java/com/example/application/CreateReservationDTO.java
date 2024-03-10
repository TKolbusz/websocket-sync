package com.example.application;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;

import java.time.LocalDateTime;

@Serdeable
public record CreateReservationDTO(
        @NonNull LocalDateTime startAt,
        @NonNull LocalDateTime endAt,
        @NonNull String customer
) {

}
