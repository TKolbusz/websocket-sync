package com.example.application;

import com.example.domain.ReservationService;
import com.example.model.Reservation;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@MicronautTest
public class ReservationControllerTest {

    @Inject
    ReservationService reservationService;

    @MockBean(ReservationService.class)
    ReservationService designService() {
        return Mockito.mock(ReservationService.class);
    }

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void testCreateReservation() {
        // given
        Reservation savedReservation = new Reservation("test-id", "tenant1", LocalDateTime.now(), LocalDateTime.now().plusHours(1), "Alice");
        Mockito.when(reservationService.addReservation(any(), any(), any(), any())).thenReturn(savedReservation);

        CreateReservationDTO reservationDTO = new CreateReservationDTO(savedReservation.startAt(), savedReservation.endAt(), savedReservation.customer());

        // when
        HttpRequest<CreateReservationDTO> request = HttpRequest.POST("/tenant1/reservations", reservationDTO);
        HttpResponse<ReservationDTO> response = client.toBlocking().exchange(request, ReservationDTO.class);

        // then
        assertEquals(HttpStatus.CREATED, response.status());
        assertNotNull(response.body());
        assertEquals("test-id", response.body().id());
    }
}
