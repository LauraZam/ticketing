package kz.kimep.ticketing.booking.api;

import jakarta.validation.Valid;
import kz.kimep.ticketing.booking.service.SeatHoldService;
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
@RequestMapping("/api/v1/holds")
@RequiredArgsConstructor
public class SeatHoldController {

    private final SeatHoldService seatHoldService;

    @PostMapping
    public ResponseEntity<HoldResponse> createHold(
            @Valid @RequestBody CreateHoldRequest request,
            @AuthenticationPrincipal User user) {
        HoldResponse response = seatHoldService.holdSeats(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}