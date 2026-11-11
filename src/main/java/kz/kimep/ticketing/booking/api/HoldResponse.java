package kz.kimep.ticketing.booking.api;

import kz.kimep.ticketing.booking.domain.SeatHoldStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;

@Getter
@Builder
public class HoldResponse {
    private String holdReference;
    private Long eventId;
    private Long userId;
    private Set<Long> seatIds;
    private Instant expiresAt;
    private SeatHoldStatus status;
}