package com.example.domain;

import com.example.domain.change.ChangeNotificationService;
import com.example.model.Reservation;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@MicronautTest
public class ReservationServiceTest {

    @Test
    void testAddReservation() {
        // given
        ChangeNotificationService changeNotificationServiceMock = mock(ChangeNotificationService.class);
        ReservationService reservationService = new ReservationService(changeNotificationServiceMock);

        String tenantId = "tenant1";
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(1);
        String customerName = "Alice";

        // when
        Reservation reservation = reservationService.addReservation(tenantId, startTime, endTime, customerName);

        // then
        assertNotNull(reservation);
        assertNotNull(reservation.id());

        verify(changeNotificationServiceMock).processChange(tenantId, reservation.id(), "RESERVATION");
    }
}
