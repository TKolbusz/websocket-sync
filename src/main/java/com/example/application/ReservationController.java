package com.example.application;

import com.example.domain.ReservationService;
import com.example.model.Reservation;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;

@Controller("/{tenantId}/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Post("/")
    public HttpResponse<ReservationDTO> createReservation(@PathVariable("tenantId") String tenantId, @Body CreateReservationDTO reservation) {
        Reservation savedReservation = reservationService.addReservation(tenantId, reservation.startAt(), reservation.endAt(), reservation.customer());
        ReservationDTO dto = new ReservationDTO(
                savedReservation.id(),
                savedReservation.startAt(),
                savedReservation.endAt(),
                savedReservation.customer()
        );
        return HttpResponse.created(dto);
    }
}
