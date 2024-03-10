package com.example.domain;

import com.example.domain.change.ChangeNotificationService;
import com.example.model.Reservation;
import jakarta.inject.Singleton;

import java.time.LocalDateTime;
import java.util.UUID;

@Singleton
public class ReservationService {
    private final ChangeNotificationService changeNotificationService;

    public ReservationService(ChangeNotificationService changeNotificationService) {
        this.changeNotificationService = changeNotificationService;
    }

    public Reservation addReservation(
            String tenantId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String customer
    ) {
        Reservation reservation = new Reservation(
                UUID.randomUUID().toString(),
                tenantId,
                startAt,
                endAt,
                customer
        );
        changeNotificationService.processChange(tenantId, reservation.id(), "RESERVATION");
        return reservation;
    }

}
