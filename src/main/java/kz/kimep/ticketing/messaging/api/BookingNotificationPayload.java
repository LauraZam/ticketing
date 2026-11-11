package kz.kimep.ticketing.messaging.api;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BookingNotificationPayload {
    private String bookingReference;
    private Long userId;
    private Long eventId;
    private BigDecimal totalAmount;
}