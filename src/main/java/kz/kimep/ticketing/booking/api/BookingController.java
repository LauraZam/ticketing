package kz.kimep.ticketing.booking.api;

import jakarta.validation.Valid;
import kz.kimep.ticketing.booking.service.BookingService;
import kz.kimep.ticketing.identity.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal User user) {
        BookingResponse response = bookingService.createBooking(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}