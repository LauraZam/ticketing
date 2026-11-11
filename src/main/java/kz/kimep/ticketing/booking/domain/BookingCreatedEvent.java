package kz.kimep.ticketing.booking.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class BookingCreatedEvent {
    private String bookingReference;
    private Long userId;
    private Long eventId;
    private BigDecimal totalAmount;
}